package com.bbc.km.configuration;

import com.bbc.km.compound.PlateKitchenMenuItemCompound;
import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.dto.notify.PlateOrdersNotifyDTO;
import com.bbc.km.dto.notify.PlateOrdersNotifyItem;
import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.jpa.entity.OrderAck;
import com.bbc.km.service.KitchenMenuItemService;
import com.bbc.km.service.PlateService;
import com.bbc.km.jpa.service.OrderAckService;
import com.bbc.km.websocket.PKMINotification;
import com.bbc.km.websocket.PKMINotificationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static com.bbc.km.configuration.PostgresConfig.DATASOURCE;

@Component
public class ServletContextListenerImpl implements ServletContextListener {

    private static final String NOTIFICATION_TOPIC = "/topic/pkmi";

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextListenerImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PGConnection pgConnection;

    private boolean isChannelOpen = false;

    @Value("${application.menu-item-notes-separator:/}")
    private String menuItemNoteSeparator;
    @Value("${application.enable-orders-auto-insert:false}")
    private Boolean enableOrdersAutoInsert;
    
    @Autowired
    private PlateKitchenMenuItemCompound pkmiCompound;
    @Autowired
    private KitchenMenuItemService kmiService;
    @Autowired
    private OrderAckService orderAckService;
    @Autowired
    private PlateService plateService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public ServletContextListenerImpl(@Autowired @Qualifier(DATASOURCE) DataSource dataSource) throws SQLException {
        pgConnection = (PGConnection) dataSource.getConnection();

        pgConnection.addNotificationListener(new PGNotificationListener() {
            @Override
            public void notification(int processId, String channelName, String payload) {
                LOGGER.info("Received from channel {} message with payload {}", channelName, payload);
                final JsonNode json;
                try {
                    json = OBJECT_MAPPER.readTree(payload);
                    PlateOrdersNotifyDTO notifyDTO = OBJECT_MAPPER.treeToValue(json, PlateOrdersNotifyDTO.class);

                    // update OrdersAck table in PG
                    Optional<OrderAck> orderAckOp = orderAckService.getOrderById(notifyDTO.getItem().getId());
                    if (orderAckOp.isPresent()) {
                        OrderAck orderAck = orderAckOp.get();
                        orderAck.setAck(true);
                        orderAckService.saveOrder(orderAck);
                    }

                    PlateKitchenMenuItemDTO pkmiDto = this.mapPlateKitchenMenuItemDTO(notifyDTO.getItem());
                    for (int i = 0; i < notifyDTO.getItem().getQuantity(); i++) {
                        if (notifyDTO.getItem().getMenuItemNotes() != null && !notifyDTO.getItem().getMenuItemNotes().isEmpty()) {
                            String[] menuItemNotes = notifyDTO.getItem().getMenuItemNotes().split(menuItemNoteSeparator);
                            this.setMenuItemNotes(pkmiDto, menuItemNotes, i);
                        }

                        PlateKitchenMenuItemDTO resultDto = pkmiCompound.create(pkmiDto);

                        PKMINotification notification = new PKMINotification();
                        notification.setType(PKMINotificationType.PKMI_ADD);
                        notification.setPlateKitchenMenuItem(resultDto);
                        simpMessagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);
                    }
                } catch (JsonProcessingException e) {
                    LOGGER.error("Failed json processing for ingested payload!", e);
                }
            }

            private void setMenuItemNotes(PlateKitchenMenuItemDTO pkmiDto, String[] notes, int i) {
                if (notes.length > 0) {
                    String currentNote = (i < notes.length) ? notes[i] : "";
                    pkmiDto.setNotes(currentNote.trim());
                }
            }

            private PlateKitchenMenuItemDTO mapPlateKitchenMenuItemDTO(PlateOrdersNotifyItem notifyItem) {
                PlateKitchenMenuItemDTO result = new PlateKitchenMenuItemDTO();
                KitchenMenuItem kmi = kmiService.getItemByExternalId(notifyItem.getMenuItemId());

                result.setMenuItem(kmi);
                result.setStatus(ItemStatus.TODO);
                result.setOrderNumber(notifyItem.getOrderNumber());
                result.setTableNumber(notifyItem.getTableNumber());
                result.setClientName(notifyItem.getClientName());
                result.setTakeAway(notifyItem.getTakeAway());
                result.setOrderNotes(notifyItem.getOrderNotes());

                // auto order insert
                if (this.enableOrdersAutoInsert) {
                    Plate plate = this.retrievePlateFromCategory(kmi);
                    result.setPlate(plate);
                    // update order status based 
                    result.setStatus(ItemStatus.PROGRESS);
                    if (plate.getSlot().get(0) >= plate.getSlot().get(1)) {
                        LOGGER.info("Plate {} full, queue order into ", plate.getName());
                        result.setStatus(ItemStatus.TODO);
                    }
                }

                return result;
            }

            private Plate retrievePlateFromCategory(KitchenMenuItem kmi) {
                String categoryId = kmi.getCategoryId();
                Plate result = plateService.findCandidatePlate(categoryId);
                return result;
            }
        });
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Statement statement = pgConnection.createStatement();
            statement.execute("LISTEN plate_orders");
            statement.close();
            isChannelOpen = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (isChannelOpen)
            try {
                Statement statement = pgConnection.createStatement();
                statement.execute("UNLISTEN plate_orders");
                statement.close();
                isChannelOpen = false;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

    }


}
