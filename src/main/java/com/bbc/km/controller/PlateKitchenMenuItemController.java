package com.bbc.km.controller;

import com.bbc.km.compound.CompoundController;
import com.bbc.km.compound.PlateKitchenMenuItemCompound;
import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.websocket.PKMINotification;
import com.bbc.km.websocket.PKMINotificationType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/plate-item"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class PlateKitchenMenuItemController implements CompoundController<String, PlateKitchenMenuItemDTO> {

    private static final String NOTIFICATION_TOPIC = "/topic/pkmi";

    private final PlateKitchenMenuItemCompound compound;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public PlateKitchenMenuItemController(PlateKitchenMenuItemCompound compound, SimpMessagingTemplate simpMessagingTemplate) {
        this.compound = compound;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<PlateKitchenMenuItemDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(compound.getById(id));
    }

    @GetMapping("/ids")
    public ResponseEntity<List<PlateKitchenMenuItemDTO>> getByIds(@RequestBody List<String> ids) {
        return ResponseEntity.ok(compound.getByIds(ids));
    }

    @GetMapping
    @Override
    public ResponseEntity<List<PlateKitchenMenuItemDTO>> getAll() {
        return ResponseEntity.ok(compound.getAll());
    }

    @PostMapping
    public ResponseEntity<PlateKitchenMenuItemDTO> create(@RequestBody PlateKitchenMenuItemDTO pkmiDto) {
        PlateKitchenMenuItemDTO dto = compound.create(pkmiDto);

        PKMINotification notification = new PKMINotification();
        notification.setType(PKMINotificationType.PKMI_ADD);
        notification.setPlateKitchenMenuItem(dto);
        simpMessagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/list")
    @Override
    public ResponseEntity<List<PlateKitchenMenuItemDTO>> createAll(@RequestBody List<PlateKitchenMenuItemDTO> pkmiList) {
        List<PlateKitchenMenuItemDTO> dtoList = compound.createAll(pkmiList);

        PKMINotification notification = new PKMINotification();
        notification.setType(PKMINotificationType.PKMI_ADD_ALL);
        notification.setIds(dtoList.stream().map(PlateKitchenMenuItemDTO::getId).collect(Collectors.toList()));
        simpMessagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);

        return ResponseEntity.ok(dtoList);
    }

    @PutMapping
    public ResponseEntity<PlateKitchenMenuItemDTO> update(@RequestBody PlateKitchenMenuItemDTO pkmiDto) {
        PlateKitchenMenuItemDTO dto = compound.update(pkmiDto);

        PKMINotification notification = new PKMINotification();
        notification.setType(PKMINotificationType.PKMI_UPDATE);
        notification.setPlateKitchenMenuItem(dto);
        simpMessagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/list")
    @Override
    public ResponseEntity<List<PlateKitchenMenuItemDTO>> updateAll(@RequestBody List<PlateKitchenMenuItemDTO> pkmiList) {
        List<PlateKitchenMenuItemDTO> dtoList = compound.updateAll(pkmiList);

        PKMINotification notification = new PKMINotification();
        notification.setType(PKMINotificationType.PKMI_UPDATE_ALL);
        notification.setIds(dtoList.stream().map(PlateKitchenMenuItemDTO::getId).collect(Collectors.toList()));
        simpMessagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);

        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping("/list")
    @Override
    public ResponseEntity<Map<String, Boolean>> deleteAll(@RequestBody List<String> ids) {
        Map<String, Boolean> resultMap = compound.deleteAll(ids);

        PKMINotification notification = new PKMINotification();
        notification.setType(PKMINotificationType.PKMI_DELETE_ALL);
        notification.setIds(new ArrayList<>(resultMap.keySet()));
        simpMessagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);

        return ResponseEntity.ok(resultMap);
    }
}
