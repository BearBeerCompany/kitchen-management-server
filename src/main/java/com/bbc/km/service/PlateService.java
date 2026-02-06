package com.bbc.km.service;

import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.exception.ObjectNotFoundException;
import com.bbc.km.exception.PlateNotEmptyException;
import com.bbc.km.model.Category;
import com.bbc.km.model.Plate;
import com.bbc.km.repository.CategoryRepository;
import com.bbc.km.repository.PlateRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlateService extends CRUDService<String, Plate> {

    private final PlateKitchenMenuItemService plateKitchenMenuItemService;
    private final CategoryRepository categoryRepository;
    private final PlateRepository plateRepository;

    public PlateService(PlateRepository plateRepository,
                        final @Lazy PlateKitchenMenuItemService plateKitchenMenuItemService,
                        final @Lazy CategoryRepository categoryRepository) {
        super(plateRepository);
        this.plateKitchenMenuItemService = plateKitchenMenuItemService;
        this.categoryRepository = categoryRepository;
    }

    @Override
    protected String validateOnCreate(Plate dto) {
        StringBuilder builder = new StringBuilder();

        if (dto.getName() == null) {
            builder.append("Name cannot be null!");
        }

        if (dto.getColor() == null) {
            builder.append("Color cannot be null!");
        }
        // Check categories if exists
        if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
            checkCategoryIdsValid(dto.getCategories(), builder);
        }

        return builder.toString();
    }

    @Override
    protected List<String> validateAllOnCreate(List<Plate> plates) {
        throw new UnsupportedOperationException("Plate multiple validation not supported");
    }

    @Override
    protected String validateOnUpdate(Plate dto) {
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
        // Check categories if exists
        if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
            checkCategoryIdsValid(dto.getCategories(), builder);
        }

        return builder.toString();
    }

    private void checkCategoryIdsValid(final List<String> categoryIds, final StringBuilder messageBuilder) {

        final List<CategoryRepository.CategoryIdProjection> existingProjections = categoryRepository.findIdsByIdIn(categoryIds);
        // Extract IDs into a Set for fast lookup
        Set<String> existingIds = existingProjections.stream()
                .map(CategoryRepository.CategoryIdProjection::getId)
                .collect(Collectors.toSet());

        for (String id : categoryIds) {
            if (!existingIds.contains(id)) {
                messageBuilder.append(String.format("Missing category ID: %s ", id));
            }
        }
    }

    public void decrementCounterById(String id) {
        Optional<Plate> optionalItem = repository.findById(id);

        if (optionalItem.isPresent()) {
            Plate plate = optionalItem.get();
            plate.setSlot(List.of(
                    plate.getSlot().get(0) - 1,
                    plate.getSlot().get(1)));
            repository.save(plate);
        } else {
            throw new ObjectNotFoundException(id);
        }
    }

    public void incrementCounterById(String id) {
        Optional<Plate> optionalItem = repository.findById(id);

        if (optionalItem.isPresent()) {
            Plate plate = optionalItem.get();
            plate.setSlot(List.of(
                    plate.getSlot().get(0) + 1,
                    plate.getSlot().get(1)));
            repository.save(plate);
        } else {
            throw new ObjectNotFoundException(id);
        }
    }

    @Override
    public Plate update(Plate plate) {

        String errors = validateOnUpdate(plate);

        if (errors.isEmpty()) {
//            Optional<Plate> optionalItem = repository.findById(plate.getId());
//
//            if (optionalItem.isPresent()) {
//                plate.setSlot(List.of(
//                        optionalItem.get().getSlot().get(0),
//                        plate.getSlot().get(1)));
                return repository.save(plate);
//            } else {
//                throw new ObjectNotFoundException(plate.getId());
//            }
        } else {
            throw new RuntimeException(errors);
        }
    }

    public Plate patchEnable(final String id, final Boolean enable) {
        List<PlateKitchenMenuItemDTO> items = plateKitchenMenuItemService.findByPlateId(id);

        if (!enable && !items.isEmpty()) {
            throw new PlateNotEmptyException("The plate cannot be turned off, please remove all items before shutting down");
        }

        final Plate plate = super.getById(id);
        plate.setEnabled(enable);
        return super.update(plate);
    }

    public Plate findFirstFreePlate(String categoryId) {
        PlateRepository plateRepository = (PlateRepository) repository;
        return plateRepository.findFreePlatesByCategory(categoryId, Sort.by("slot.0").ascending()).stream().findFirst();
    }

    public Plate findCandidatePlate(String categoryId) {
        PlateRepository plateRepository = (PlateRepository) repository;
        Sort sort = Sort.by(
            Sort.Order.asc("slot.0"),   // meno carica
            Sort.Order.desc("slot.1")   // a parità, più capiente
        );
        return plateRepository.findCandidatePlates(categoryId, Sort.by("slot.0").ascending())
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No plate associated to category " + categoryId));
    }

}
