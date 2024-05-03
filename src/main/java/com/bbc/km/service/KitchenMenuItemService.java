package com.bbc.km.service;

import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.repository.KitchenMenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class KitchenMenuItemService extends CRUDService<String, KitchenMenuItem> {

    protected KitchenMenuItemService(KitchenMenuItemRepository repository) {
        super(repository);
    }

    public List<KitchenMenuItem> getItemsByCategoryId(String categoryId) {
        Objects.requireNonNull(categoryId, "Category Id cannot be null!");

        return ((KitchenMenuItemRepository) repository).findByCategoryId(categoryId);
    }

    public KitchenMenuItem getItemByExternalId(Integer externalId) {
        Objects.requireNonNull(externalId, "External id cannot be null!");
        KitchenMenuItem item = null;

        List<KitchenMenuItem> list = ((KitchenMenuItemRepository) repository).findByExternalId(externalId);
        if (!list.isEmpty()) {
            item = list.get(0);
        }
        return item;
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
