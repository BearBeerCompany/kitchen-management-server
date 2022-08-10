package com.bbc.km.service;

import com.bbc.km.model.PlateKitchenMenuItem;
import com.bbc.km.repository.PlateKitchenMenuItemRepository;
import org.springframework.stereotype.Service;

@Service
public class PlateKitchenMenuItemService extends CRUDService<String, PlateKitchenMenuItem> {

    protected PlateKitchenMenuItemService(PlateKitchenMenuItemRepository repository) {
        super(repository);
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

        if (dto.getPlateId() == null) {
            builder.append("Plate Id cannot be null!");
        }

        if (dto.getMenuItemId() == null) {
            builder.append("Item Id cannot be null!");
        }

        if (dto.getOrderNumber() == null) {
            builder.append("Order number cannot be null!");
        }

        if (dto.getStatus() == null) {
            builder.append("Status cannot be null!");
        }

        return builder.toString();
    }
}
