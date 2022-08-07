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
        Optional<DTO> optionalCategory = repository.findById(id);

        if (optionalCategory.isPresent()) {
            return optionalCategory.get();
        } else {
            throw new ObjectNotFoundException(id);
        }
    }

    public List<DTO> getAll() {
        return repository.findAll();
    }

    public DTO create(DTO dto) {
        String errors = validateOnCreate(dto);

        if (errors.length() == 0) {
            return repository.insert(dto);
        } else {
            throw new RuntimeException(errors);
        }
    }

    public DTO update(DTO dto) {

        String errors = validateOnUpdate(dto);

        if (errors.length() == 0) {
            Optional<DTO> optionalPlate = repository.findById(dto.getId());

            if (optionalPlate.isPresent()) {
                return repository.save(dto);
            } else {
                throw new ObjectNotFoundException(dto.getId());
            }
        } else {
            throw new RuntimeException(errors);
        }
    }

    public void delete(ID id) {
        Optional<DTO> optionalPlate = repository.findById(id);

        optionalPlate.ifPresentOrElse(repository::delete,
                () -> {
                    throw new ObjectNotFoundException(id);
                });
    }

    protected abstract String validateOnCreate(DTO dto);

    protected abstract String validateOnUpdate(DTO dto);
}
