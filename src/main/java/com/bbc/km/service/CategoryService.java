package com.bbc.km.service;

import com.bbc.km.model.Category;
import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.repository.CategoryRepository;
import com.bbc.km.repository.KitchenMenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService extends CRUDService<String, Category> {

    private final KitchenMenuItemRepository kitchenMenuItemRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           KitchenMenuItemRepository kitchenMenuItemRepository) {
        super(categoryRepository);
        this.kitchenMenuItemRepository = kitchenMenuItemRepository;
    }

    @Override
    public Category delete(String id) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be null!");
        }

        List<KitchenMenuItem> categoryItems = kitchenMenuItemRepository.findByCategoryId(id);
        kitchenMenuItemRepository.deleteAll(categoryItems);

        return super.delete(id);
    }

    @Override
    public void deleteAll() {
        kitchenMenuItemRepository.deleteAll();
        super.deleteAll();
    }

    @Override
    protected String validateOnCreate(Category dto) {
        StringBuilder builder = new StringBuilder();

        if (dto == null) {
            return "DTO cannot be null!";
        }

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        return builder.toString();
    }

    @Override
    protected List<String> validateAllOnCreate(List<Category> dtos) {
        // todo
        return List.of();
    }

    @Override
    protected String validateOnUpdate(Category dto) {
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

        return builder.toString();
    }
}
