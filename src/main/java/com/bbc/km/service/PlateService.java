package com.bbc.km.service;

import com.bbc.km.exception.ObjectNotFoundException;
import com.bbc.km.model.Plate;
import com.bbc.km.repository.PlateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlateService implements CRUDService<String, Plate> {

    private final PlateRepository plateRepository;

    public PlateService(PlateRepository plateRepository) {
        this.plateRepository = plateRepository;
    }

    @Override
    public Plate getById(String id) {

        Optional<Plate> optionalPlate = plateRepository.findById(id);

        if (optionalPlate.isPresent()) {
            return optionalPlate.get();
        } else {
            throw new ObjectNotFoundException(id);
        }
    }

    @Override
    public List<Plate> getAll() {
        return plateRepository.findAll();
    }

    @Override
    public Plate create(Plate dto) {

        StringBuilder builder = new StringBuilder();

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        if (dto.getColor() == null) {
            builder.append("Color cannot be null!");
        }

        if (builder.length() == 0) {
            return plateRepository.insert(dto);
        } else {
            throw new RuntimeException(builder.toString());
        }

    }

    @Override
    public Plate update(Plate dto) {

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

        if (builder.length() == 0) {
            Optional<Plate> optionalPlate = plateRepository.findById(dto.getId());

            if (optionalPlate.isPresent()) {
                return plateRepository.save(dto);
            } else {
                throw new ObjectNotFoundException(dto.getId());
            }
        } else {
            throw new RuntimeException(builder.toString());
        }
    }

    @Override
    public void delete(String id) {

        Optional<Plate> optionalPlate = plateRepository.findById(id);

        optionalPlate.ifPresentOrElse(plateRepository::delete,
                () -> {
                    throw new ObjectNotFoundException(id);
                });
    }
}
