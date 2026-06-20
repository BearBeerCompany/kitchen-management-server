package com.bbc.km.configuration;

// import com.bbc.km.compound.PlateKitchenMenuItemCompound;
import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.dto.notify.PlateOrdersNotifyDTO;
import com.bbc.km.dto.notify.PlateOrdersNotifyItem;
import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.model.Plate;
import com.bbc.km.model.PlateKitchenMenuItem;
import com.bbc.km.jpa.entity.OrderAck;
import com.bbc.km.service.KitchenMenuItemService;
import com.bbc.km.service.PlateKitchenMenuItemService;
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
import org.springframework.scheduling.annotation.Scheduled;
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
    private static final String CHANNEL = "plate_orders";

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextListenerImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * The LISTEN connection is a single, long-lived direct (non-pooled) pgjdbc-ng connection. If the
     * underlying socket dies (db restart, network blip, idle connection reaped by a firewall/NAT) it would
     * silently stop delivering notifications until the application is restarted. To avoid that, the connection
     * is (re)created lazily and a scheduled health-check validates it periodically (the validation query also
     * acts as a keepalive, preventing idle reaping) and reconnects + re-issues LISTEN when needed.
     */
    private final DataSource dataSource;
    private final Object connectionLock = new Object();
    private volatile PGConnection pgConnection;
    private volatile boolean isChannelOpen = false;

    @Value("${application.menu-item-notes-separator:/}")
    private String menuItemNoteSeparator;
    @Value("${application.enable-orders-auto-insert:false}")
    private Boolean enableOrdersAutoInsert;

    // @Autowired
    // private PlateKitchenMenuItemCompound pkmiCompound;
    @Autowired
    private PlateKitchenMenuItemService pkmiService;
    @Autowired
    private KitchenMenuItemService kmiService;
    @Autowired
    private OrderAckService orderAckService;
    @Autowired
    private PlateService plateService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public ServletContextListenerImpl(@Autowired @Qualifier(DATASOURCE) DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private final PGNotificationListener notificationListener = new PGNotificationListener() {
        @Override
        public void notification(int processId, String channelName, String payload) {
            LOGGER.info("Received from channel {} message with payload {}", channelName, payload);
            final JsonNode json;
            try {
                json = OBJECT_MAPPER.readTree(payload);
                PlateOrdersNotifyDTO notifyDTO = OBJECT_MAPPER.treeToValue(json, PlateOrdersNotifyDTO.class);

                // resolve the menu item before consuming the order: if the item referenced by GSG is
                // not present here (e.g. menu not imported yet), skip WITHOUT acknowledging so the batch
                // OrderAckProcessingJob can retry it later, instead of failing with NPE.
                KitchenMenuItem kmi = kmiService.getItemByExternalId(notifyDTO.getItem().getMenuItemId());
                if (kmi == null) {
                    LOGGER.warn("ServletContextListenerImpl::notification - no kitchen menu item found for external id {} (order {}, table {}); skipping, will be retried by batch job",
                            notifyDTO.getItem().getMenuItemId(), notifyDTO.getItem().getOrderNumber(), notifyDTO.getItem().getTableNumber());
                    return;
                }

                // update OrdersAck table in PG
                Optional<OrderAck> orderAckOp = orderAckService.getOrderById(notifyDTO.getItem().getId());
                if (orderAckOp.isPresent()) {
                    OrderAck orderAck = orderAckOp.get();
                    orderAck.setAck(true);
                    orderAckService.saveOrder(orderAck);
                }

                for (int i = 0; i < notifyDTO.getItem().getQuantity(); i++) {
                    PlateKitchenMenuItem pkmiDto = this.mapPlateKitchenMenuItem(notifyDTO.getItem(), kmi);
                    if (notifyDTO.getItem().getMenuItemNotes() != null && !notifyDTO.getItem().getMenuItemNotes().isEmpty()) {
                        String[] menuItemNotes = notifyDTO.getItem().getMenuItemNotes().split(menuItemNoteSeparator);
                        this.setMenuItemNotes(pkmiDto, menuItemNotes, i);
                    }

                    PlateKitchenMenuItem result = pkmiService.create(pkmiDto);
                    PlateKitchenMenuItemDTO resultDto = doc2Dto(result);

                    PKMINotification notification = new PKMINotification();
                    notification.setType(PKMINotificationType.PKMI_ADD);
                    notification.setPlateKitchenMenuItem(resultDto);
                    simpMessagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);
                }
            } catch (JsonProcessingException e) {
                LOGGER.error("Failed json processing for ingested payload!", e);
            }
        }

        @Override
        public void closed() {
            // pgjdbc-ng signals the LISTEN connection was closed: flag it so the next health-check reconnects.
            LOGGER.warn("ServletContextListenerImpl - LISTEN connection on channel {} was closed; will reconnect on next health-check", CHANNEL);
            isChannelOpen = false;
        }

        private void setMenuItemNotes(PlateKitchenMenuItem pkmi, String[] notes, int i) {
            if (notes.length > 0) {
                String currentNote = (i < notes.length) ? notes[i] : "";
                pkmi.setNotes(currentNote.trim());
            }
        }

        private PlateKitchenMenuItem mapPlateKitchenMenuItem(PlateOrdersNotifyItem notifyItem, KitchenMenuItem kmi) {
            PlateKitchenMenuItem result = new PlateKitchenMenuItem();

            result.setMenuItemId(kmi.getId());
            result.setStatus(ItemStatus.TODO);
            result.setOrderNumber(notifyItem.getOrderNumber());
            result.setTableNumber(notifyItem.getTableNumber());
            result.setClientName(notifyItem.getClientName());
            result.setTakeAway(notifyItem.getTakeAway());
            result.setOrderNotes(notifyItem.getOrderNotes());

            // auto order insert
            if (ServletContextListenerImpl.this.enableOrdersAutoInsert) {
                Plate plate = this.retrievePlateFromCategory(kmi);
                result.setPlateId(plate.getId());
                // update order status based
                // result.setStatus(ItemStatus.PROGRESS);
                // if (plate.getSlot().get(0) >= plate.getSlot().get(1)) {
                //    LOGGER.info("Plate {} full, queue order into ", plate.getName());
                //    result.setStatus(ItemStatus.TODO);
                // }
            }

            return result;
        }

        private Plate retrievePlateFromCategory(KitchenMenuItem kmi) {
            String categoryId = kmi.getCategoryId();
            Plate result = plateService.findCandidatePlate(categoryId);
            return result;
        }

        private PlateKitchenMenuItemDTO doc2Dto(PlateKitchenMenuItem doc) {
            PlateKitchenMenuItemDTO dto = new PlateKitchenMenuItemDTO();
            String menuItemId = doc.getMenuItemId();
            String plateId = doc.getPlateId();

            // retrieve menuItem data
            KitchenMenuItem kmiDoc = kmiService.getById(menuItemId);
            // retrieve plate data
            Plate plate = (plateId != null) ? plateService.getById(plateId) : null;

            dto.setId(doc.getId());
            dto.setMenuItem(kmiDoc);
            dto.setPlate(plate);
            dto.setOrderNumber(doc.getOrderNumber());
            dto.setClientName(doc.getClientName());
            dto.setStatus(doc.getStatus());
            dto.setTableNumber(doc.getTableNumber());
            dto.setNotes(doc.getNotes());
            dto.setOrderNotes(doc.getOrderNotes());
            dto.setCreatedDate(doc.getCreatedDate());
            dto.setTakeAway(doc.getTakeAway());
            return dto;
        }
    };

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        connectAndListen();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        synchronized (connectionLock) {
            closeQuietly();
        }
    }

    /**
     * Periodically validates the LISTEN connection and reconnects when it is not healthy. The validation
     * query doubles as a keepalive that prevents idle connection reaping by firewalls/NAT.
     */
    @Scheduled(fixedDelayString = "${application.jobs.listener-health-check.fixedDelay:30000}")
    public void healthCheck() {
        synchronized (connectionLock) {
            if (isConnectionHealthy()) {
                return;
            }
            LOGGER.warn("ServletContextListenerImpl::healthCheck - LISTEN connection on channel {} not healthy, reconnecting", CHANNEL);
            connectAndListen();
        }
    }

    private boolean isConnectionHealthy() {
        if (!isChannelOpen || pgConnection == null) {
            return false;
        }
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("SELECT 1");
            return true;
        } catch (SQLException e) {
            LOGGER.warn("ServletContextListenerImpl::isConnectionHealthy - validation query failed: {}", e.getMessage());
            return false;
        }
    }

    private void connectAndListen() {
        synchronized (connectionLock) {
            closeQuietly();
            try {
                PGConnection connection = (PGConnection) dataSource.getConnection();
                connection.addNotificationListener(notificationListener);
                try (Statement statement = connection.createStatement()) {
                    statement.execute("LISTEN " + CHANNEL);
                }
                pgConnection = connection;
                isChannelOpen = true;
                LOGGER.info("ServletContextListenerImpl - listening on channel {}", CHANNEL);
            } catch (SQLException e) {
                isChannelOpen = false;
                LOGGER.error("ServletContextListenerImpl - failed to (re)connect LISTEN on channel {}, will retry at next health-check", CHANNEL, e);
            }
        }
    }

    private void closeQuietly() {
        if (pgConnection == null) {
            return;
        }
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("UNLISTEN " + CHANNEL);
        } catch (SQLException e) {
            LOGGER.debug("ServletContextListenerImpl - UNLISTEN failed (connection likely already dead): {}", e.getMessage());
        }
        try {
            pgConnection.close();
        } catch (SQLException e) {
            LOGGER.debug("ServletContextListenerImpl - error closing previous pg connection: {}", e.getMessage());
        } finally {
            pgConnection = null;
            isChannelOpen = false;
        }
    }
}
