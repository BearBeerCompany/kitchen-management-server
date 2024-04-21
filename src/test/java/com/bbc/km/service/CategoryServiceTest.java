package com.bbc.km.service;

import com.bbc.km.model.Category;
import com.bbc.km.repository.CategoryRepository;
import com.bbc.km.repository.KitchenMenuItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    // mocks
    private static final KitchenMenuItemRepository KITCHEN_MENU_ITEM_REPOSITORY = Mockito.mock(KitchenMenuItemRepository.class);
    private static final CategoryRepository CATEGORY_REPOSITORY = Mockito.mock(CategoryRepository.class);
    // internals
    private static final String CATEGORY_ID = "MM93";
    private static final String CATEGORY_NAME = "MarcMarquez";
    // tested
    private final CategoryService categoryService = new CategoryService(CATEGORY_REPOSITORY, KITCHEN_MENU_ITEM_REPOSITORY);

    @AfterEach
    void afterEach() {
        Mockito.reset(CATEGORY_REPOSITORY, KITCHEN_MENU_ITEM_REPOSITORY);
    }

    @Nested
    @DisplayName("mock test for create method")
    class CreateTestGroup {
        @Test
        void service_callCreate_shouldValidateAndSave() {
            final Category new_category = new Category();
            new_category.setName(CATEGORY_NAME);

            Mockito.when(CATEGORY_REPOSITORY.insert(Mockito.eq(new_category)))
                    .thenAnswer(invocationOnMock -> {
                        Category saved_category = new Category();
                        saved_category.setId(CATEGORY_ID);
                        saved_category.setName(CATEGORY_NAME);
                        return saved_category;
                    });

            Assertions.assertNull(new_category.getId());
            final Category saved_category = categoryService.create(new_category);

            Assertions.assertNotNull(saved_category);
            Assertions.assertEquals(CATEGORY_ID, saved_category.getId());
            Assertions.assertEquals(CATEGORY_NAME, saved_category.getName());
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
            Mockito.verify(CATEGORY_REPOSITORY, Mockito.times(1)).insert(Mockito.eq(new_category));
        }

        @Test
        void service_callCreate_withInvalidInput_shouldThrowException() {
            final Category new_category = new Category();
            new_category.setName(null);

            final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> categoryService.create(new_category));

            Assertions.assertNotNull(exception);
            Assertions.assertTrue(exception.getMessage().contains("Name cannot be null!"));
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
            Mockito.verifyNoInteractions(CATEGORY_REPOSITORY);
        }

        @Test
        void service_callCreate_withNullRequest_shouldThrowException() {
            final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> categoryService.create(null));

            Assertions.assertNotNull(exception);
            Assertions.assertEquals("DTO cannot be null!", exception.getMessage());
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
            Mockito.verifyNoInteractions(CATEGORY_REPOSITORY);
        }
    }


    @Nested
    @DisplayName("mock test for update method")
    class UpdateTestGroup {
        @Test
        void service_callUpdate_shouldValidateAndSave() {
            final Category edited_category = new Category();
            edited_category.setName(CATEGORY_NAME);
            edited_category.setId(CATEGORY_ID);

            Mockito.when(CATEGORY_REPOSITORY.findById(Mockito.eq(CATEGORY_ID)))
                    .thenReturn(Optional.of(edited_category));

            Mockito.when(CATEGORY_REPOSITORY.save(Mockito.eq(edited_category)))
                    .thenAnswer(invocationOnMock -> {
                        Category saved_category = new Category();
                        saved_category.setId(CATEGORY_ID);
                        saved_category.setName(CATEGORY_NAME);
                        return saved_category;
                    });

            final Category saved_category = categoryService.update(edited_category);

            Assertions.assertNotNull(saved_category);
            Assertions.assertEquals(CATEGORY_ID, saved_category.getId());
            Assertions.assertEquals(CATEGORY_NAME, saved_category.getName());
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
            Mockito.verify(CATEGORY_REPOSITORY, Mockito.times(1))
                    .findById(Mockito.eq(CATEGORY_ID));
            Mockito.verify(CATEGORY_REPOSITORY, Mockito.times(1))
                    .save(Mockito.eq(edited_category));
        }

        @Test
        void service_callUpdate_withInvalidInput_shouldThrowException() {
            final Category new_category = new Category();
            new_category.setName(null);
            new_category.setId(null);

            final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> categoryService.update(new_category));

            Assertions.assertNotNull(exception);
            Assertions.assertTrue(exception.getMessage().contains("Name cannot be null!"));
            Assertions.assertTrue(exception.getMessage().contains("Id cannot be null!"));
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
            Mockito.verifyNoInteractions(CATEGORY_REPOSITORY);
        }

        @Test
        void service_callUpdate_withNullRequest_shouldThrowException() {
            final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> categoryService.update(null));

            Assertions.assertNotNull(exception);
            Assertions.assertEquals("DTO cannot be null!", exception.getMessage());
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
            Mockito.verifyNoInteractions(CATEGORY_REPOSITORY);
        }
    }

    @Nested
    @DisplayName("mock test for delete method")
    class DeleteTestGroup {
        @Test
        void service_callUpdate_shouldValidateAndSave() {

            Mockito.when(KITCHEN_MENU_ITEM_REPOSITORY.findByCategoryId(Mockito.eq(CATEGORY_ID)))
                    .thenReturn(new LinkedList<>());

            Mockito.when(CATEGORY_REPOSITORY.findById(Mockito.eq(CATEGORY_ID)))
                    .thenAnswer(invocationOnMock -> {
                        Category deleted = new Category();
                        deleted.setId(CATEGORY_ID);
                        return Optional.of(deleted);
                    });


            final Category deleted_category = categoryService.delete(CATEGORY_ID);

            Assertions.assertNotNull(deleted_category);
            Assertions.assertEquals(CATEGORY_ID, deleted_category.getId());

            Mockito.verify(CATEGORY_REPOSITORY, Mockito.times(1))
                    .findById(Mockito.eq(CATEGORY_ID));
            Mockito.verify(KITCHEN_MENU_ITEM_REPOSITORY, Mockito.times(1))
                    .findByCategoryId(Mockito.eq(CATEGORY_ID));
            Mockito.verify(KITCHEN_MENU_ITEM_REPOSITORY, Mockito.times(1))
                    .deleteAll(Mockito.anyList());
            Mockito.verify(CATEGORY_REPOSITORY, Mockito.times(1))
                    .deleteById(Mockito.eq(CATEGORY_ID));
        }

        @Test
        void service_callDelete_withoutId_shouldThrowException() {
            final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> categoryService.delete(null));

            Assertions.assertNotNull(exception);
            Assertions.assertTrue(exception.getMessage().contains("Id cannot be null!"));
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
            Mockito.verifyNoInteractions(CATEGORY_REPOSITORY);
        }

        @Test
        void service_callDelete_withBlankId_shouldThrowException() {
            final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> categoryService.delete("              "));

            Assertions.assertNotNull(exception);
            Assertions.assertTrue(exception.getMessage().contains("Id cannot be null!"));
            Mockito.verifyNoInteractions(KITCHEN_MENU_ITEM_REPOSITORY);
            Mockito.verifyNoInteractions(CATEGORY_REPOSITORY);
        }
    }
}
