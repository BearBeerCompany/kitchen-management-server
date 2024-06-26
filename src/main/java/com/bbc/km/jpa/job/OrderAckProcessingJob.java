package com.bbc.km.jpa.job;

import com.bbc.km.compound.PlateKitchenMenuItemCompound;
import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.jpa.entity.OrderAck;
import com.bbc.km.jpa.service.OrderAckService;
import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.service.KitchenMenuItemService;
import com.bbc.km.websocket.PKMINotification;
import com.bbc.km.websocket.PKMINotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Component
public class OrderAckProcessingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAckProcessingJob.class);
    private static final String NOTIFICATION_TOPIC = "/topic/pkmi";
    private final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Value("${application.jobs.order-ack.interval:120000}")
    private Integer interval;
    @Value("${application.menu-item-notes-separator:/}")
    private String menuItemNoteSeparator;

    @Autowired
    private PlateKitchenMenuItemCompound pkmiCompound;
    @Autowired
    private KitchenMenuItemService kmiService;
    @Autowired
    private OrderAckService orderAckService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Scheduled(fixedDelayString = "${application.jobs.order-ack.fixedDelay:60000}")
    public void processOrders() {
        // Scansiona i record non confermati
        List<OrderAck> unacknowledgedOrders = orderAckService.getUnAckOrders();
        ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Rome"));
        LOGGER.info("OrderAckProcessingJob::processOrders start @ {}, #unacknowledgedOrders: {}", currentDateTime, unacknowledgedOrders.size());

        if (!unacknowledgedOrders.isEmpty()) {
            // Verifica se i record non confermati superano l'intervallo temporale stabilito
            for (OrderAck order : unacknowledgedOrders) {
                if (isTimeElapsed(order, currentDateTime)) {
                    // Imposta la conferma su true e salva il record aggiornato nel database
                    order.setAck(true);
                    orderAckService.saveOrder(order);

                    // Notifica il FE tramite web socket
                    PlateKitchenMenuItemDTO pkmiDto = this.mapPlateKitchenMenuItemDTO(order);
                    for (int i = 0; i < order.getQuantity(); i++) {
                        if (order.getMenuItemNotes() != null && !order.getMenuItemNotes().isEmpty()) {
                            String[] menuItemNotes = order.getMenuItemNotes().split(menuItemNoteSeparator);
                            this.setMenuItemNotes(pkmiDto, menuItemNotes, i);
                        }

                        PlateKitchenMenuItemDTO resultDto = pkmiCompound.create(pkmiDto);

                        PKMINotification notification = new PKMINotification();
                        notification.setType(PKMINotificationType.PKMI_ADD);
                        notification.setPlateKitchenMenuItem(resultDto);
                        simpMessagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);
                    }
                }
            }
        }

        LOGGER.info("OrderAckProcessingJob::processOrders end");
    }

    private boolean isTimeElapsed(OrderAck order, ZonedDateTime currentDateTime) {
        // Implementa la logica per verificare se è trascorso un intervallo di tempo sufficiente
        String insertTime = order.getInsertTime().substring(0, order.getInsertTime().indexOf('.'));
        String orderDateTime = order.getInsertDate() + " " + insertTime;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.ITALY);
        LocalDateTime orderLocalDateTime = LocalDateTime.parse(orderDateTime, dateTimeFormatter);
        ZoneId zoneId = ZoneId.of("Europe/Rome");
        ZonedDateTime zonedDateTime = ZonedDateTime.of(orderLocalDateTime, zoneId);
        Long diff = zonedDateTime.until(currentDateTime, ChronoUnit.MILLIS);
        boolean isElapsed = diff > interval;
        LOGGER.info("OrderAckProcessingJob::isTimeElapsed {}, zonedDateTime @ {}, #currentDateTime: {}, diff {}",
                isElapsed,
                zonedDateTime,
                currentDateTime,
                diff);
        return isElapsed;
    }

    private PlateKitchenMenuItemDTO mapPlateKitchenMenuItemDTO(OrderAck order) {
        PlateKitchenMenuItemDTO result = new PlateKitchenMenuItemDTO();
        KitchenMenuItem kmi = kmiService.getItemByExternalId(order.getMenuItemId());
        result.setMenuItem(kmi);
        result.setStatus(ItemStatus.TODO);
        result.setOrderNumber(order.getOrderNumber());
        result.setTableNumber(order.getTableNumber());
        result.setClientName(order.getClientName());
        result.setTakeAway(order.getTakeAway());
        result.setOrderNotes(order.getOrderNotes());
        return result;
    }

    private void setMenuItemNotes(PlateKitchenMenuItemDTO pkmiDto, String[] notes, int i) {
        if (notes.length > 0) {
            String currentNote = (i < notes.length) ? notes[i] : "";
            pkmiDto.setNotes(currentNote);
        }
    }
}
