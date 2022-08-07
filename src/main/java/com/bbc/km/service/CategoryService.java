package com.bbc.km.service;

import com.bbc.km.model.Category;
import com.bbc.km.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends CRUDService<String, Category> {

    public CategoryService(CategoryRepository categoryRepository) {
        super(categoryRepository);
    }

    @Override
    protected String validateOnCreate(Category dto) {
        StringBuilder builder = new StringBuilder();

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        return builder.toString();
    }

    @Override
    protected String validateOnUpdate(Category dto) {
        StringBuilder builder = new StringBuilder();

        if (dto.getId() == null) {
            builder.append("Id cannot be null!");
        }

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        return builder.toString();
    }
}
