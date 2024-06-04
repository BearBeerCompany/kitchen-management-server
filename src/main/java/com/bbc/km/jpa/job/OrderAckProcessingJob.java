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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Component
public class OrderAckProcessingJob {

    private static final String NOTIFICATION_TOPIC = "/topic/pkmi";
    private final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    @Value("${application.jobs.order-ack.interval:120000}")
    private Integer interval;

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
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Verifica se i record non confermati superano l'intervallo temporale stabilito
        for (OrderAck order : unacknowledgedOrders) {
            if (isTimeElapsed(order, currentDateTime)) {
                // Imposta la conferma su true e salva il record aggiornato nel database
                order.setAck(true);
                orderAckService.saveOrder(order);

                // Notifica il FE tramite web socket
                PlateKitchenMenuItemDTO pkmiDto = this.mapPlateKitchenMenuItemDTO(order);
                for (int i = 0; i < order.getQuantity(); i++) {
                    PlateKitchenMenuItemDTO resultDto = pkmiCompound.create(pkmiDto);

                    PKMINotification notification = new PKMINotification();
                    notification.setType(PKMINotificationType.PKMI_ADD);
                    notification.setPlateKitchenMenuItem(resultDto);
                    simpMessagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);
                }
            }
        }
    }

    private boolean isTimeElapsed(OrderAck order, LocalDateTime currentDateTime) {
        // Implementa la logica per verificare se è trascorso un intervallo di tempo sufficiente
        String orderDateTime = order.getInsertDate() + " " + order.getInsertTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.ITALY);
        LocalDateTime orderLocalDateTime = LocalDateTime.parse(orderDateTime, dateTimeFormatter);
        return orderLocalDateTime.until(currentDateTime, ChronoUnit.MILLIS) > interval;
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
        result.setNotes(order.getOrderNotes()); // todo capire quali usare, se globali o specifiche
        // todo createdDate
        return result;
    }
}
