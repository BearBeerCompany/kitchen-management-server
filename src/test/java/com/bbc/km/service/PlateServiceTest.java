package com.bbc.km.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Collections;

import com.bbc.km.model.Plate;
import com.bbc.km.repository.CategoryRepository;
import com.bbc.km.repository.PlateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PlateServiceTest {

    private PlateService plateService;
    private CategoryRepository categoryRepository;
    private PlateKitchenMenuItemService plateKitchenMenuItemService;

    @BeforeEach
    void setup() {
        categoryRepository = Mockito.mock(CategoryRepository.class);
        plateKitchenMenuItemService = Mockito.mock(PlateKitchenMenuItemService.class);
        plateService = new PlateService(
                Mockito.mock(PlateRepository.class),
                plateKitchenMenuItemService,
                categoryRepository
        );
    }

    @Test
    void validateOnCreate_WithNullNameAndColor_ReturnsErrorMessages() {
        Plate plate = new Plate();
        String result = plateService.validateOnCreate(plate);

        assertEquals("Name cannot be null!Color cannot be null!", result);
    }

    @Test
    void validateOnCreate_WithEmptyCategoryList_NoCategoryError() {
        Plate plate = new Plate();
        plate.setName("Pizza");
        plate.setColor("Red");
        plate.setCategories(Collections.emptyList());

        String result = plateService.validateOnCreate(plate);

        assertEquals("", result); // No error messages expected
    }

    @Test
    void validateOnCreate_WithAllCategoriesExisting_NoMissingCategoryError() {
        Plate plate = new Plate();
        plate.setName("Pizza");
        plate.setColor("Red");
        plate.setCategories(List.of("cat1", "cat2"));

        // Mock repository to return all IDs
        when(categoryRepository.findIdsByIdIn(List.of("cat1", "cat2")))
                .thenReturn(List.of(
                        createProjection("cat1"),
                        createProjection("cat2")
                ));

        String result = plateService.validateOnCreate(plate);

        assertEquals("", result); // All categories exist
    }

    @Test
    void validateOnCreate_WithSomeCategoriesMissing_ReturnsMissingCategoryError() {
        Plate plate = new Plate();
        plate.setName("Pizza");
        plate.setColor("Red");
        plate.setCategories(List.of("cat1", "cat2", "cat3"));

        // Mock repository to return only some IDs
        when(categoryRepository.findIdsByIdIn(List.of("cat1", "cat2", "cat3")))
                .thenReturn(List.of(
                        createProjection("cat2")
                ));

        String result = plateService.validateOnCreate(plate);

        // Only cat1 and cat3 are missing
        assertEquals("Missing category ID: cat1 Missing category ID: cat3 ", result);
    }

    @Test
    void validateOnUpdate_WithNullId_ReturnsIdError() {
        Plate plate = new Plate();
        plate.setName("Burger");
        plate.setColor("Yellow");
        plate.setId(null);

        String result = plateService.validateOnUpdate(plate);

        assertEquals("Id cannot be null!", result);
    }

    // Helper method to create CategoryIdProjection
    private CategoryRepository.CategoryIdProjection createProjection(String id) {
        return () -> id;
    }
}
