package com.bbc.km.service;

import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.repository.KitchenMenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

@Service
public class KitchenMenuItemService extends CRUDService<String, KitchenMenuItem> {

    protected KitchenMenuItemService(KitchenMenuItemRepository repository) {
        super(repository);
    }

    public List<KitchenMenuItem> getItemsByCategoryId(String categoryId) {
        Assert.notNull(categoryId, "Category Id cannot be null!");
        //TODO: validate also id if exists real category, using category cache
        return ((KitchenMenuItemRepository) repository).findByCategoryId(categoryId);
    }

    public KitchenMenuItem getItemByExternalIndex(Integer externalIndex) {
        Objects.requireNonNull(externalIndex, "External Index cannot be null!");
        KitchenMenuItem item = null;

        List<KitchenMenuItem> list = ((KitchenMenuItemRepository) repository).findByExternalIndex(externalIndex);
        if (!list.isEmpty()) {
            item = list.get(0);
        }
        return item;
    }

    @Override
    protected String validateOnCreate(KitchenMenuItem dto) {
        StringBuilder builder = new StringBuilder();

        if (dto == null) {
            return "DTO cannot be null!";
        }

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        if (dto.getCategoryId() == null) {
            builder.append("Category Id cannot be null!");
        }

        //TODO: validate also id if exists real category, using category cache

        return builder.toString();
    }

    @Override
    protected String validateOnUpdate(KitchenMenuItem dto) {
        StringBuilder builder = new StringBuilder();

        if (dto == null) {
            return "DTO cannot be null!";
        }

        if (dto.getId() == null) {
            builder.append("Id cannot be null!");
        }

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        if (dto.getCategoryId() == null) {
            builder.append("Category Id cannot be null!");
        }

        //TODO: validate also id if exists real category, using category cache

        return builder.toString();
    }
}
