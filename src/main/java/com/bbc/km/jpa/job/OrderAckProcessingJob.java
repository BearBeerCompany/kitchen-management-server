package com.bbc.km.jpa.job;

// import com.bbc.km.compound.PlateKitchenMenuItemCompound;
import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.jpa.entity.OrderAck;
import com.bbc.km.jpa.service.OrderAckService;
import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.model.PlateKitchenMenuItem;
import com.bbc.km.service.KitchenMenuItemService;
import com.bbc.km.service.PlateKitchenMenuItemService;
import com.bbc.km.websocket.PKMINotification;
import com.bbc.km.websocket.PKMINotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import com.bbc.km.model.Plate;

@Component
public class OrderAckProcessingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAckProcessingJob.class);
    private static final String NOTIFICATION_TOPIC = "/topic/pkmi";
    private final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Value("${application.jobs.order-ack.interval:120000}")
    private Integer interval;
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
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private com.bbc.km.service.PlateService plateService;

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

                    for (int i = 0; i < order.getQuantity(); i++) {
                        PlateKitchenMenuItem pkmiDto = this.mapPlateKitchenMenuItem(order);
                        if (order.getMenuItemNotes() != null && !order.getMenuItemNotes().isEmpty()) {
                            String[] menuItemNotes = order.getMenuItemNotes().split(menuItemNoteSeparator);
                            this.setMenuItemNotes(pkmiDto, menuItemNotes, i);
                        }

                        // PlateKitchenMenuItemDTO resultDto = pkmiCompound.create(pkmiDto);
                        LOGGER.info("OrderAckProcessingJob::processOrders create pkmi id {}, plateId: {}, order: {}, table: {}", pkmiDto.getId(), pkmiDto.getPlateId(), pkmiDto.getOrderNumber(), pkmiDto.getTableNumber());
                        PlateKitchenMenuItem result = pkmiService.create(pkmiDto);
                        PlateKitchenMenuItemDTO resultDto = doc2Dto(result);

                        // Notifica il FE tramite web socket
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

        // auto order insert
        if (this.enableOrdersAutoInsert) {
            Plate plate = this.retrievePlateFromCategory(kmi);
            result.setPlate(plate);
            // update order status based 
            result.setStatus(ItemStatus.PROGRESS);
            if (plate.getSlot().get(0) >= plate.getSlot().get(1)) {
                LOGGER.info("OrderAckProcessingJob::mapPlateKitchenMenuItemDTO - Plate {} full, queue order into ", plate.getName());
                result.setStatus(ItemStatus.TODO);
            }
        }

        return result;
    }

    private PlateKitchenMenuItem mapPlateKitchenMenuItem(OrderAck order) {
        PlateKitchenMenuItem result = new PlateKitchenMenuItem();
        KitchenMenuItem kmi = kmiService.getItemByExternalId(order.getMenuItemId());

        result.setMenuItemId(kmi.getId());
        result.setStatus(ItemStatus.TODO);
        result.setOrderNumber(order.getOrderNumber());
        result.setTableNumber(order.getTableNumber());
        result.setClientName(order.getClientName());
        result.setTakeAway(order.getTakeAway());
        result.setOrderNotes(order.getOrderNotes());

        // auto order insert
        if (this.enableOrdersAutoInsert) {
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

    private void setMenuItemNotes(PlateKitchenMenuItem pkmi, String[] notes, int i) {
        if (notes.length > 0) {
            String currentNote = (i < notes.length) ? notes[i] : "";
            pkmi.setNotes(currentNote);
        }
    }

    private Plate retrievePlateFromCategory(KitchenMenuItem kmi) {
        String categoryId = kmi.getCategoryId();
        Plate result = plateService.findCandidatePlate(categoryId);
        return result;
    }
    
}
