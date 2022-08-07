package com.bbc.km.controller;

import com.bbc.km.model.Category;
import com.bbc.km.service.CategoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/category"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class CategoryController extends RESTController<String, Category> {

    public CategoryController(CategoryService categoryService) {
        super(categoryService);
    }
}
