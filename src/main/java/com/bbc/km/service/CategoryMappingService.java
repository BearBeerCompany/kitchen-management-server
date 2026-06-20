package com.bbc.km.service;

import com.bbc.km.model.CategoryMapping;
import com.bbc.km.repository.CategoryMappingRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryMappingService extends CRUDService<String, CategoryMapping> {

    public CategoryMappingService(CategoryMappingRepository repository) {
        super(repository);
    }

    /**
     * Item-level overrides as {@code GSG Articolo.id -> target GSG Tipologia.id}.
     */
    public Map<Integer, Integer> getItemOverrides() {
        Map<Integer, Integer> result = new HashMap<>();
        for (CategoryMapping mapping : repository.findAll()) {
            if (mapping.getExternalItemId() != null && mapping.getTargetExternalCategoryId() != null) {
                result.put(mapping.getExternalItemId(), mapping.getTargetExternalCategoryId());
            }
        }
        return result;
    }

    /**
     * Category-level overrides as {@code source GSG Tipologia.id -> target GSG Tipologia.id}.
     */
    public Map<Integer, Integer> getCategoryOverrides() {
        Map<Integer, Integer> result = new HashMap<>();
        for (CategoryMapping mapping : repository.findAll()) {
            if (mapping.getSourceExternalCategoryId() != null && mapping.getTargetExternalCategoryId() != null) {
                result.put(mapping.getSourceExternalCategoryId(), mapping.getTargetExternalCategoryId());
            }
        }
        return result;
    }

    @Override
    protected String validateOnCreate(CategoryMapping dto) {
        return validate(dto);
    }

    @Override
    protected List<String> validateAllOnCreate(List<CategoryMapping> dtos) {
        return List.of();
    }

    @Override
    protected String validateOnUpdate(CategoryMapping dto) {
        StringBuilder builder = new StringBuilder();
        if (dto != null && dto.getId() == null) {
            builder.append("Id cannot be null!");
        }
        builder.append(validate(dto));
        return builder.toString();
    }

    private String validate(CategoryMapping dto) {
        StringBuilder builder = new StringBuilder();

        if (dto == null) {
            return "DTO cannot be null!";
        }

        if (dto.getTargetExternalCategoryId() == null) {
            builder.append("Target external category id cannot be null!");
        }

        boolean hasItem = dto.getExternalItemId() != null;
        boolean hasCategory = dto.getSourceExternalCategoryId() != null;
        if (hasItem == hasCategory) {
            builder.append("Exactly one of externalItemId (item-level) or sourceExternalCategoryId (category-level) must be set!");
        }

        return builder.toString();
    }
}
