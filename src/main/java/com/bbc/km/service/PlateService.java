package com.bbc.km.service;

import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.exception.PlateNotEmptyException;
import com.bbc.km.model.Plate;
import com.bbc.km.repository.PlateKitchenMenuItemRepository;
import com.bbc.km.repository.PlateRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlateService extends CRUDService<String, Plate> {

    private final PlateKitchenMenuItemService plateKitchenMenuItemService;

    public PlateService(PlateRepository plateRepository,
                        @Lazy PlateKitchenMenuItemService plateKitchenMenuItemService) {
        super(plateRepository);
        this.plateKitchenMenuItemService = plateKitchenMenuItemService;
    }

    @Override
    protected String validateOnCreate(Plate dto) {
        StringBuilder builder = new StringBuilder();

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        if (dto.getColor() == null) {
            builder.append("Color cannot be null!");
        }

        return builder.toString();
    }

    @Override
    protected String validateOnUpdate(Plate dto) {
        StringBuilder builder = new StringBuilder();

        if (dto.getId() == null) {
            builder.append("Id cannot be null!");
        }

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        if (dto.getColor() == null) {
            builder.append("Color cannot be null!");
        }

        return builder.toString();
    }

    public Plate patchEnable(final String id, final Boolean enable) {
        List<PlateKitchenMenuItemDTO> items = plateKitchenMenuItemService.findByPlateId(id);

        if(!enable && !items.isEmpty()) {
            throw new PlateNotEmptyException("The plate cannot be turned off, please remove all items before shutting down");
        }

        final Plate plate = super.getById(id);
        plate.setEnabled(enable);
        return super.update(plate);
    }
}
