package com.bbc.km.service;

import com.bbc.km.controller.ErrorHandlerController;
import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.exception.PlateOffException;
import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.Plate;
import com.bbc.km.model.PlateKitchenMenuItem;
import com.bbc.km.repository.PlateKitchenMenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class PlateKitchenMenuItemService extends CRUDService<String, PlateKitchenMenuItem> {

    private final PlateService plateService;
    private final StatsService statsService;

    protected PlateKitchenMenuItemService(PlateKitchenMenuItemRepository repository,
                                          PlateService plateService,
                                          StatsService statsService) {
        super(repository);
        this.plateService = plateService;
        this.statsService = statsService;
    }

    public List<PlateKitchenMenuItemDTO> findByPlateId(String id) {
        Objects.requireNonNull(id, "Plate id cannot be null!");

        return ((PlateKitchenMenuItemRepository) repository).findByPlateId(id);
    }

    public List<PlateKitchenMenuItemDTO> findByPlateIdNull() {
        return ((PlateKitchenMenuItemRepository) repository).findByPlateIdNull();
    }

    @Override
    public PlateKitchenMenuItem create(PlateKitchenMenuItem plateKitchenMenuItem) {
        Plate plate = null;
        int currentItems = 0;
        if (plateKitchenMenuItem.getPlateId() != null) {
            plate = plateService.getById(plateKitchenMenuItem.getPlateId());
            if (plate.getEnabled()) {
                currentItems = plate.getSlot().get(0);
                int maxItems = plate.getSlot().get(1);

                if (currentItems < maxItems) {
                    plateKitchenMenuItem.setStatus(ItemStatus.PROGRESS);
                    currentItems++;
                }
            } else {
                throw new PlateOffException("Selected plate is not enabled yet",
                        new ErrorHandlerController.ErrorInfo(plateKitchenMenuItem.getPlateId()));
            }
        }

        PlateKitchenMenuItem result = super.create(plateKitchenMenuItem);
        if (result.getStatus().equals(ItemStatus.PROGRESS)) {
            plate.getSlot().set(0, currentItems);
            plateService.update(plate);
        }

        statsService.update(null, result.getStatus());
        return result;
    }

    @Override
    public PlateKitchenMenuItem update(PlateKitchenMenuItem plateKitchenMenuItem) {
        // previous
        PlateKitchenMenuItem previousItem = this.getById(plateKitchenMenuItem.getId());
        ItemStatus previousItemStatus = previousItem.getStatus();
        String previousItemPlateId = previousItem.getPlateId();
        Plate previousPlate = null;
        boolean previousPlateChanged = false;

        // next
        ItemStatus nextItemStatus = plateKitchenMenuItem.getStatus();
        String nextItemPlateId = plateKitchenMenuItem.getPlateId();
        Plate nextPlate = null;
        boolean nextPlateChanged = false;

        if (previousItemPlateId != null && previousItemPlateId.equals(plateKitchenMenuItem.getPlateId())) {
            // same plate
            if (!nextItemStatus.equals(previousItemStatus)) {
                // status changed
                nextPlate = plateService.getById(plateKitchenMenuItem.getPlateId());
                if (!nextPlate.getEnabled()) {
                    throw new PlateOffException("Selected plate is not enabled yet",
                            new ErrorHandlerController.ErrorInfo(plateKitchenMenuItem.getPlateId()));
                }

                int currentItems = nextPlate.getSlot().get(0);
                int maxItems = nextPlate.getSlot().get(1);

                if (nextItemStatus.equals(ItemStatus.PROGRESS)) {
                    // check if almost one slot is free in the current plate
                    if (currentItems < maxItems) {
                        // there's a free slot in the current plate, increment the items counter
                        plateKitchenMenuItem.setStatus(ItemStatus.PROGRESS);
                        currentItems++;
                        nextPlate.getSlot().set(0, currentItems);
                        nextPlateChanged = true;
                    } else {
                        // no slot free in current plate, queue the item
                        plateKitchenMenuItem.setStatus(ItemStatus.TODO);
                        // todo: notify user that the plate is full
                    }
                } else if (previousItemStatus.equals(ItemStatus.PROGRESS)) {
                    // decrement items counter
                    currentItems--;
                    nextPlate.getSlot().set(0, currentItems);
                    nextPlateChanged = true;
                }
            }
        } else {
            // item moved from one to plate to another
            if (previousItemPlateId != null) {
                if (previousItemStatus.equals(ItemStatus.PROGRESS)) {
                    // the item was in progress in the previous plate, so decrement its counter
                    previousPlate = plateService.getById(previousItemPlateId);
                    int currentItems = previousPlate.getSlot().get(0);
                    currentItems--;
                    previousPlate.getSlot().set(0, currentItems);
                    previousPlateChanged = true;
                }
            }

            if (nextItemPlateId != null) {

                nextPlate = plateService.getById(plateKitchenMenuItem.getPlateId());
                if (!nextPlate.getEnabled()) {
                    throw new PlateOffException("Selected plate is not enabled yet",
                            new ErrorHandlerController.ErrorInfo(plateKitchenMenuItem.getPlateId()));
                }

                // item moved to an existing plate
                if (nextItemStatus.equals(ItemStatus.PROGRESS)) {
                    // check if almost one slot is free in the next plate, otherwise queue the item
                    int currentItems = nextPlate.getSlot().get(0);
                    int maxItems = nextPlate.getSlot().get(1);
                    if (currentItems < maxItems) {
                        // free slot in the next plate
                        currentItems++;
                        nextPlate.getSlot().set(0, currentItems);
                        nextPlateChanged = true;
                    } else {
                        // no free slot in current plate, queue the item in the next plate
                        plateKitchenMenuItem.setStatus(ItemStatus.TODO);
                        // todo: notify user that the plate is full
                    }
                }
            }
        }

        PlateKitchenMenuItem result = super.update(plateKitchenMenuItem);

        if (previousPlateChanged) {
            plateService.update(previousPlate);
        }
        if (nextPlateChanged) {
            plateService.update(nextPlate);
        }
        statsService.update(previousItemStatus, result.getStatus());
        return result;
    }

    @Override
    public PlateKitchenMenuItem delete(String s) {
        // todo
        statsService.update(this.getById(s).getStatus(), ItemStatus.CANCELLED);
        return super.delete(s);
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
