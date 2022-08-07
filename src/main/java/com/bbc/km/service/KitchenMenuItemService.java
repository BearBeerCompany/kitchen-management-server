package com.bbc.km.service;

import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.repository.KitchenMenuItemRepository;
import org.springframework.stereotype.Service;

@Service
public class KitchenMenuItemService extends CRUDService<String, KitchenMenuItem> {

    protected KitchenMenuItemService(KitchenMenuItemRepository repository) {
        super(repository);
    }

    @Override
    protected String validateOnCreate(KitchenMenuItem dto) {
        StringBuilder builder = new StringBuilder();

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        if (dto.getCategoryId() == null) {
            builder.append("Category Id cannot be null!");
        }

        return builder.toString();
    }

    @Override
    protected String validateOnUpdate(KitchenMenuItem dto) {
        StringBuilder builder = new StringBuilder();

        if (dto.getId() == null) {
            builder.append("Id cannot be null!");
        }

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        if (dto.getCategoryId() == null) {
            builder.append("Category Id cannot be null!");
        }

        return builder.toString();
    }
}
