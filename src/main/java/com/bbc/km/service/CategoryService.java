package com.bbc.km.service;

import com.bbc.km.exception.ObjectNotFoundException;
import com.bbc.km.model.Category;
import com.bbc.km.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService implements CRUDService<String, Category> {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category getById(String id) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);

        if (optionalCategory.isPresent()) {
            return optionalCategory.get();
        } else {
            throw new ObjectNotFoundException(id);
        }
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category create(Category dto) {

        StringBuilder builder = new StringBuilder();

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        if (builder.length() == 0) {
            return categoryRepository.insert(dto);
        } else {
            throw new RuntimeException(builder.toString());
        }
    }

    @Override
    public Category update(Category dto) {

        StringBuilder builder = new StringBuilder();

        if (dto.getId() == null) {
            builder.append("Id cannot be null!");
        }

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        if (builder.length() == 0) {
            Optional<Category> optionalPlate = categoryRepository.findById(dto.getId());

            if (optionalPlate.isPresent()) {
                return categoryRepository.save(dto);
            } else {
                throw new ObjectNotFoundException(dto.getId());
            }
        } else {
            throw new RuntimeException(builder.toString());
        }
    }

    @Override
    public void delete(String id) {
        Optional<Category> optionalPlate = categoryRepository.findById(id);

        optionalPlate.ifPresentOrElse(categoryRepository::delete,
                () -> {
                    throw new ObjectNotFoundException(id);
                });
    }
}
