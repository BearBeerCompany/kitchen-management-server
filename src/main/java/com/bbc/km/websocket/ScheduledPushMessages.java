package com.bbc.km.websocket;

import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.PlateKitchenMenuItem;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ScheduledPushMessages {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public ScheduledPushMessages(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Scheduled(fixedRate = 5000)
    public void sendMessage() {
        PlateKitchenMenuItem pkmi = new PlateKitchenMenuItem();
        pkmi.setId("1");
        pkmi.setMenuItemId("2");
        pkmi.setOrderNumber(3);
        pkmi.setPlateId("4");
        pkmi.setStatus(ItemStatus.TODO);
        pkmi.setTableNumber(10);
        pkmi.setNotes("note");
        pkmi.setClientName("Boz");

        PKMINotification notification = new PKMINotification();
        notification.setType(PKMINotificationType.PKMI_ADD);
        notification.setPlateKitchenMenuItem(pkmi);
        simpMessagingTemplate.convertAndSend("/topic/pkmi", notification);
    }
}
