package com.bbc.km.service;

import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.repository.KitchenMenuItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class KitchenMenuItemServiceTest {
    // mocks
    private static final KitchenMenuItemRepository KITCHEN_MENU_ITEM_REPOSITORY = Mockito.mock(KitchenMenuItemRepository.class);
    // internals
    private static final String ITEM_ID = "LH44";
    private static final String ITEM_NAME = "MoneyTrees";
    private static final String CATEGORY_ID = "Hit the brakes when they on patrol";
    // tested
    private final KitchenMenuItemService kitchenMenuItemService = new KitchenMenuItemService(KITCHEN_MENU_ITEM_REPOSITORY);

    @AfterEach
    void afterEach() {
        Mockito.reset(KITCHEN_MENU_ITEM_REPOSITORY);
    }

    @Test
    void service_getItemsByCategoryId_shouldReturnAllItems() {

        Mockito.when(KITCHEN_MENU_ITEM_REPOSITORY.findByCategoryId(Mockito.eq(CATEGORY_ID)))
                .thenAnswer(invocationOnMock -> {
                    final KitchenMenuItem kmi = new KitchenMenuItem();
                    kmi.setId(ITEM_ID);
                    kmi.setCategoryId(CATEGORY_ID);
                    return Collections.singletonList(kmi);
                });

        final List<KitchenMenuItem> result = kitchenMenuItemService.getItemsByCategoryId(CATEGORY_ID);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(CATEGORY_ID, result.get(0).getCategoryId());
        Mockito.verify(KITCHEN_MENU_ITEM_REPOSITORY, Mockito.times(1))
                .findByCategoryId(Mockito.eq(CATEGORY_ID));
    }

    @Test
    void service_getItemsByCategoryId_nullInput_shouldThrowException() {
        final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> kitchenMenuItemService.getItemsByCategoryId(null));

        Assertions.assertNotNull(exception);
        Assertions.assertTrue(exception.getMessage().contains("Category Id cannot be null!"));
        Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
    }

    @Nested
    @DisplayName("mock test for create method")
    class CreateTestGroup {
        @Test
        void service_callCreate_shouldValidateAndSave() {
            final KitchenMenuItem new_item = new KitchenMenuItem();
            new_item.setName(ITEM_NAME);
            new_item.setCategoryId(CATEGORY_ID);

            Mockito.when(KITCHEN_MENU_ITEM_REPOSITORY.insert(Mockito.eq(new_item)))
                    .thenAnswer(invocationOnMock -> {
                        new_item.setId(ITEM_ID);
                        return new_item;
                    });

            Assertions.assertNull(new_item.getId());
            final KitchenMenuItem save_item = kitchenMenuItemService.create(new_item);

            Assertions.assertNotNull(save_item);
            Assertions.assertEquals(CATEGORY_ID, save_item.getCategoryId());
            Assertions.assertEquals(ITEM_ID, save_item.getId());
            Assertions.assertEquals(ITEM_NAME, save_item.getName());
            Mockito.verify(KITCHEN_MENU_ITEM_REPOSITORY, Mockito.times(1)).insert(Mockito.eq(new_item));
        }

        @Test
        void service_callCreate_withInvalidInput_shouldThrowException() {
            final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> kitchenMenuItemService.create(new KitchenMenuItem()));

            Assertions.assertNotNull(exception);
            Assertions.assertTrue(exception.getMessage().contains("Name cannot be null!"));
            Assertions.assertTrue(exception.getMessage().contains("Category Id cannot be null!"));
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
        }

        @Test
        void service_callCreate_withNullRequest_shouldThrowException() {
            final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> kitchenMenuItemService.create(null));

            Assertions.assertNotNull(exception);
            Assertions.assertEquals("DTO cannot be null!", exception.getMessage());
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
        }
    }


    @Nested
    @DisplayName("mock test for update method")
    class UpdateTestGroup {
        @Test
        void service_callUpdate_shouldValidateAndSave() {
            final KitchenMenuItem new_item = new KitchenMenuItem();
            new_item.setName(ITEM_NAME);
            new_item.setCategoryId(CATEGORY_ID);
            new_item.setId(ITEM_ID);

            Mockito.when(KITCHEN_MENU_ITEM_REPOSITORY.findById(Mockito.eq(ITEM_ID)))
                    .thenReturn(Optional.of(new_item));

            Mockito.when(KITCHEN_MENU_ITEM_REPOSITORY.save(Mockito.eq(new_item)))
                    .thenReturn(new_item);

            final KitchenMenuItem saved_item = kitchenMenuItemService.update(new_item);

            Assertions.assertNotNull(saved_item);
            Assertions.assertEquals(CATEGORY_ID, saved_item.getCategoryId());
            Assertions.assertEquals(ITEM_ID, saved_item.getId());
            Mockito.verify(KITCHEN_MENU_ITEM_REPOSITORY, Mockito.times(1))
                    .findById(Mockito.eq(ITEM_ID));
            Mockito.verify(KITCHEN_MENU_ITEM_REPOSITORY, Mockito.times(1))
                    .save(Mockito.eq(new_item));
        }

        @Test
        void service_callUpdate_withInvalidInput_shouldThrowException() {
            final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> kitchenMenuItemService.update(new KitchenMenuItem()));

            Assertions.assertNotNull(exception);
            Assertions.assertTrue(exception.getMessage().contains("Name cannot be null!"));
            Assertions.assertTrue(exception.getMessage().contains("Category Id cannot be null!"));
            Assertions.assertTrue(exception.getMessage().contains("Id cannot be null!"));
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
        }

        @Test
        void service_callUpdate_withNullRequest_shouldThrowException() {
            final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> kitchenMenuItemService.update(null));

            Assertions.assertNotNull(exception);
            Assertions.assertEquals("DTO cannot be null!", exception.getMessage());
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
        }
    }


}
