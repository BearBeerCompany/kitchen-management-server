package com.bbc.km.controller;

import com.bbc.km.model.Category;
import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.service.CategoryService;
import com.bbc.km.service.KitchenMenuItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/category"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class CategoryController extends RESTController<String, Category> {

    private final KitchenMenuItemService kitchenMenuItemService;

    public CategoryController(CategoryService categoryService,
                              KitchenMenuItemService kitchenMenuItemService) {
        super(categoryService);
        this.kitchenMenuItemService = kitchenMenuItemService;
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<KitchenMenuItem>> getItemsByCategoryId(@PathVariable("id") String categoryId) {
        return ResponseEntity.ok(kitchenMenuItemService.getItemsByCategoryId(categoryId));
    }
}
