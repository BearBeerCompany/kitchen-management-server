package com.bbc.km.service;

import com.bbc.km.model.Plate;
import com.bbc.km.repository.PlateRepository;
import org.springframework.stereotype.Service;

@Service
public class PlateService extends CRUDService<String, Plate> {

    public PlateService(PlateRepository plateRepository) {
        super(plateRepository);
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
}
