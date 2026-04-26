package com.bbc.km.service;

import com.bbc.km.controller.ErrorHandlerController;
import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.exception.PlateOffException;
import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.Plate;
import com.bbc.km.model.PlateKitchenMenuItem;
import com.bbc.km.repository.PlateKitchenMenuItemJPARepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class PlateKitchenMenuItemService extends CRUDService<String, PlateKitchenMenuItem> {

    private final PlateService plateService;
    private final StatsService statsService;

    @Value("${application.priority-order-notes-regex}")
    private String priorityOrderNotesRegex;

    private Pattern priorityPattern;

    protected PlateKitchenMenuItemService(PlateKitchenMenuItemJPARepository repository,
                                          PlateService plateService,
                                          StatsService statsService) {
        super(repository);
        this.plateService = plateService;
        this.statsService = statsService;
    }

    @PostConstruct
    private void init() {
        priorityPattern = Pattern.compile(priorityOrderNotesRegex);
    }

    public boolean isPriorityNotes(String notes) {
        return notes != null && !notes.isBlank() && priorityPattern.matcher(notes).find();
    }

    private boolean isPriorityItem(PlateKitchenMenuItem item) {
        return isPriorityNotes(item.getOrderNotes());
    }

    public List<PlateKitchenMenuItemDTO> findByPlateId(String id) {
        Objects.requireNonNull(id, "Plate id cannot be null!");

        return ((PlateKitchenMenuItemJPARepository) repository).findByPlateId(id);
    }

    public List<PlateKitchenMenuItemDTO> findByPlateIdNull() {
        return ((PlateKitchenMenuItemJPARepository) repository).findByPlateIdNull();
    }

    @Override
    public PlateKitchenMenuItem create(PlateKitchenMenuItem plateKitchenMenuItem) {
        Plate plate = null;
        if (plateKitchenMenuItem.getPlateId() != null) {
            plate = plateService.getById(plateKitchenMenuItem.getPlateId());
            if (plate.getEnabled()) {
                int currentItems = plate.getSlot().get(0);
                int maxItems = plate.getSlot().get(1);

                boolean hasQueuedItems = !((PlateKitchenMenuItemJPARepository) repository).findByPlateIdAndStatusOrderByCreatedDateAsc(
                    plate.getId(),
                    ItemStatus.TODO
                ).isEmpty();
                if (!hasQueuedItems && currentItems < maxItems) {
                    plateKitchenMenuItem.setStatus(ItemStatus.PROGRESS);
                } else {
                    plateKitchenMenuItem.setStatus(ItemStatus.TODO);
                }
            } else {
                throw new PlateOffException("Selected plate is not enabled yet",
                        new ErrorHandlerController.ErrorInfo(plateKitchenMenuItem.getPlateId()));
            }
        }

        PlateKitchenMenuItem result = super.create(plateKitchenMenuItem);
        if (result.getStatus().equals(ItemStatus.PROGRESS)) {
            refreshSlot(plate);
        }

        statsService.update(plateKitchenMenuItem.getCreatedDate(), null, result.getStatus());
        return result;
    }

    @Override
    public PlateKitchenMenuItem update(PlateKitchenMenuItem plateKitchenMenuItem) {
        PlateKitchenMenuItem previousItem = this.getById(plateKitchenMenuItem.getId());
        ItemStatus previousItemStatus = previousItem.getStatus();
        String previousItemPlateId = previousItem.getPlateId();

        ItemStatus nextItemStatus = plateKitchenMenuItem.getStatus();
        String nextItemPlateId = plateKitchenMenuItem.getPlateId();

        boolean refreshNextPlate = false;
        boolean promoteNextPlate = false;
        boolean refreshPreviousPlate = false;

        if (previousItemPlateId != null && previousItemPlateId.equals(nextItemPlateId)) {
            // same plate
            if (!nextItemStatus.equals(previousItemStatus)) {
                Plate nextPlate = plateService.getById(nextItemPlateId);
                if (!nextPlate.getEnabled()) {
                    throw new PlateOffException("Selected plate is not enabled yet",
                            new ErrorHandlerController.ErrorInfo(nextItemPlateId));
                }
                int currentItems = nextPlate.getSlot().get(0);
                int maxItems = nextPlate.getSlot().get(1);

                if (nextItemStatus.equals(ItemStatus.PROGRESS)) {
                    if (currentItems < maxItems) {
                        refreshNextPlate = true;
                    } else {
                        plateKitchenMenuItem.setStatus(ItemStatus.TODO);
                    }
                } else if (previousItemStatus.equals(ItemStatus.PROGRESS)) {
                    refreshNextPlate = true;
                    promoteNextPlate = true;
                }
            }
        } else {
            // item moved from one plate to another
            if (previousItemPlateId != null && previousItemStatus.equals(ItemStatus.PROGRESS)) {
                refreshPreviousPlate = true;
            }
            if (nextItemPlateId != null) {
                Plate nextPlate = plateService.getById(nextItemPlateId);
                if (!nextPlate.getEnabled()) {
                    throw new PlateOffException("Selected plate is not enabled yet",
                            new ErrorHandlerController.ErrorInfo(nextItemPlateId));
                }
                if (nextItemStatus.equals(ItemStatus.PROGRESS)) {
                    int currentItems = nextPlate.getSlot().get(0);
                    int maxItems = nextPlate.getSlot().get(1);
                    if (currentItems < maxItems) {
                        refreshNextPlate = true;
                    } else {
                        plateKitchenMenuItem.setStatus(ItemStatus.TODO);
                    }
                }
            }
        }

        PlateKitchenMenuItem result = super.update(plateKitchenMenuItem);
        statsService.update(plateKitchenMenuItem.getCreatedDate(), previousItemStatus, result.getStatus());

        if (refreshPreviousPlate) {
            Plate previousPlate = plateService.getById(previousItemPlateId);
            refreshSlot(previousPlate);
            promoteQueuedItemsIfPossible(previousPlate);
        }
        if (refreshNextPlate) {
            Plate nextPlate = plateService.getById(nextItemPlateId);
            refreshSlot(nextPlate);
            if (promoteNextPlate) {
                promoteQueuedItemsIfPossible(nextPlate);
            }
        }

        return result;
    }

    @Override
    public PlateKitchenMenuItem delete(String s) {
        PlateKitchenMenuItem plateKitchenMenuItem = this.getById(s);
        ItemStatus itemStatus = plateKitchenMenuItem.getStatus();
        String plateId = plateKitchenMenuItem.getPlateId();

        statsService.update(plateKitchenMenuItem.getCreatedDate(), itemStatus, null);
        PlateKitchenMenuItem deleted = super.delete(s);

        if (itemStatus.equals(ItemStatus.PROGRESS) && plateId != null) {
            Plate plate = plateService.getById(plateId);
            refreshSlot(plate);
            promoteQueuedItemsIfPossible(plate);
        }

        return deleted;
    }

    @Transactional
    public void promoteQueuedItemsIfPossible(Plate plate) {
        int current = plate.getSlot().get(0);
        int max = plate.getSlot().get(1);
        if (current >= max) {
            return;
        }

        List<PlateKitchenMenuItem> queuedItems =
                ((PlateKitchenMenuItemJPARepository) repository).findByPlateIdAndStatusOrderByCreatedDateAsc(
                plate.getId(),
                ItemStatus.TODO
            );
        queuedItems.sort(Comparator.comparingInt(item -> isPriorityItem(item) ? 0 : 1));
        Iterator<PlateKitchenMenuItem> it = queuedItems.iterator();
        while (current < max && it.hasNext()) {
            PlateKitchenMenuItem item = it.next();
            item.setStatus(ItemStatus.PROGRESS);
            super.update(item);
            statsService.update(item.getCreatedDate(), ItemStatus.TODO, ItemStatus.PROGRESS);
            current++;
        }

        refreshSlot(plate);
    }

    private void refreshSlot(Plate plate) {
        long count = ((PlateKitchenMenuItemJPARepository) repository)
                .countByPlateIdAndStatus(plate.getId(), ItemStatus.PROGRESS);
        plate.getSlot().set(0, (int) count);
        plateService.update(plate);
    }


    @Override
    protected String validateOnCreate(PlateKitchenMenuItem dto) {
        StringBuilder builder = new StringBuilder();

        if (dto.getMenuItemId() == null) {
            builder.append("Item Id cannot be null!");
        }

        if (dto.getOrderNumber() == null) {
            builder.append("Order number cannot be null!");
        }

        if (dto.getTableNumber() == null) {
            builder.append("Table number cannot be null!");
        }

        if (dto.getClientName() == null || dto.getClientName().isEmpty()) {
            builder.append("Client name cannot be null or empty!");
        }

        if (dto.getStatus() == null) {
            builder.append("Status cannot be null!");
        }

        return builder.toString();
    }

    @Override
    protected List<String> validateAllOnCreate(List<PlateKitchenMenuItem> plateKitchenMenuItems) {
        // todo
        return List.of();
    }

    @Override
    protected String validateOnUpdate(PlateKitchenMenuItem dto) {
        StringBuilder builder = new StringBuilder();

        if (dto.getId() == null) {
            builder.append("Id cannot be null!");
        }

        if (dto.getMenuItemId() == null) {
            builder.append("Item Id cannot be null!");
        }

        if (dto.getOrderNumber() == null) {
            builder.append("Order number cannot be null!");
        }

        if (dto.getTableNumber() == null) {
            builder.append("Table number cannot be null!");
        }

        if (dto.getClientName() == null || dto.getClientName().isEmpty()) {
            builder.append("Client name cannot be null or empty!");
        }

        if (dto.getStatus() == null) {
            builder.append("Status cannot be null!");
        }

        return builder.toString();
    }
}
