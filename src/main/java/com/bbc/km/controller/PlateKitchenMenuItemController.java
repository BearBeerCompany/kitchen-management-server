package com.bbc.km.controller;

import com.bbc.km.compound.CompoundController;
import com.bbc.km.compound.PlateKitchenMenuItemCompound;
import com.bbc.km.dto.DetailedFilterDTO;
import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.PageResponse;
import com.bbc.km.service.PlateKitchenMenuItemService;
import com.bbc.km.websocket.PKMINotification;
import com.bbc.km.websocket.PKMINotificationType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/plate-item"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class PlateKitchenMenuItemController implements CompoundController<String, PlateKitchenMenuItemDTO> {

    private static final String NOTIFICATION_TOPIC = "/topic/pkmi";

    private final PlateKitchenMenuItemService plateKitchenMenuItemService;
    private final PlateKitchenMenuItemCompound compound;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public PlateKitchenMenuItemController(PlateKitchenMenuItemService plateKitchenMenuItemService,
                                          PlateKitchenMenuItemCompound compound,
                                          SimpMessagingTemplate simpMessagingTemplate) {
        this.plateKitchenMenuItemService = plateKitchenMenuItemService;
        this.compound = compound;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<PlateKitchenMenuItemDTO>> getUnassigned() {
        return ResponseEntity.ok(plateKitchenMenuItemService.findByPlateIdNull());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<PlateKitchenMenuItemDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(compound.getById(id));
    }

    @Override
    public ResponseEntity<List<PlateKitchenMenuItemDTO>> getAll() {
        return null;
    }

    @PostMapping("/ids")
    public ResponseEntity<List<PlateKitchenMenuItemDTO>> getByIds(@RequestBody List<String> ids) {
        return ResponseEntity.ok(compound.getByIds(ids));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PlateKitchenMenuItemDTO>> getAll(@RequestParam(name = "statuses", required = false) List<ItemStatus> statuses,
                                                                        @RequestParam(name = "offset", defaultValue = "0", required = false) Integer offset,
                                                                        @RequestParam(name = "size", defaultValue = "20", required = false) Integer size,
                                                                        @RequestParam(name = "tableNumber", required = false) String tableNumber,
                                                                        @RequestParam(name = "clientName", required = false) String clientName,
                                                                        @RequestParam(name = "categoryId", required = false) String categoryId,
                                                                        @RequestParam(name = "itemId", required = false) String itemId,
                                                                        @RequestParam(name = "orderNumber", required = false) Integer orderNumber) {
        return ResponseEntity.ok(
                compound.getAll(
                        statuses,
                        offset,
                        size,
                        new DetailedFilterDTO(tableNumber, clientName, categoryId, itemId, orderNumber)
                )
        );
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

    @DeleteMapping
    public ResponseEntity<PlateKitchenMenuItemDTO> delete(@RequestBody String id) {
        PlateKitchenMenuItemDTO result = compound.delete(id);

        PKMINotification notification = new PKMINotification();
        notification.setType(PKMINotificationType.PKMI_DELETE);
        notification.setPlateKitchenMenuItem(result);
        simpMessagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);

        return ResponseEntity.ok(result);
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
