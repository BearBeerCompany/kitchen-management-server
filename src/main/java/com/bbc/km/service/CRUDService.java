package com.bbc.km.service;

import com.bbc.km.exception.ObjectNotFoundException;
import com.bbc.km.model.MongoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public abstract class CRUDService<ID, DTO extends MongoDocument<ID>> {

    protected final MongoRepository<DTO, ID> repository;

    protected CRUDService(MongoRepository<DTO, ID> repository) {
        this.repository = repository;
    }

    public DTO getById(ID id) {

        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null!");
        }

        Optional<DTO> optionalItem = repository.findById(id);

        if (optionalItem.isPresent()) {
            return optionalItem.get();
        } else {
            throw new ObjectNotFoundException(id);
        }
    }

    public List<DTO> getAll() {
        return repository.findAll();
    }

    public DTO create(DTO dto) {
        String errors = validateOnCreate(dto);

        if (errors.isEmpty()) {
            return repository.insert(dto);
        } else {
            throw new IllegalArgumentException(errors);
        }
    }

    public List<DTO> createAll(List<DTO> dtos) {
        List<String> errors = validateAllOnCreate(dtos);

        if (errors.isEmpty()) {
            return repository.insert(dtos);
        } else {
            StringBuilder builder = new StringBuilder();
            for (String error : errors) {
                builder.append(error).append("\n");
            }
            throw new IllegalArgumentException(builder.toString());
        }
    }

    public DTO update(DTO dto) {

        String errors = validateOnUpdate(dto);

        if (errors.isEmpty()) {
            //TODO: performance improvement, remove the first query for check if document exists
            Optional<DTO> optionalItem = repository.findById(dto.getId());

            if (optionalItem.isPresent()) {
                return repository.save(dto);
            } else {
                throw new ObjectNotFoundException(dto.getId());
            }
        } else {
            throw new IllegalArgumentException(errors);
        }
    }

    public DTO delete(ID id) {

        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null!");
        }

        Optional<DTO> optionalItem = repository.findById(id);

        if (optionalItem.isPresent()) {
            repository.deleteById(id);
            return optionalItem.get();
        } else {
            throw new ObjectNotFoundException(id);
        }
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    protected abstract String validateOnCreate(DTO dto);

    protected abstract List<String> validateAllOnCreate(List<DTO> dtos);

    protected abstract String validateOnUpdate(DTO dto);
}
