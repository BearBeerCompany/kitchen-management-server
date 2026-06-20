package com.bbc.km.controller;

import com.bbc.km.model.CategoryMapping;
import com.bbc.km.service.CategoryMappingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/category-mapping"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class CategoryMappingController extends RESTController<String, CategoryMapping> {

    public CategoryMappingController(CategoryMappingService service) {
        super(service);
    }
}
