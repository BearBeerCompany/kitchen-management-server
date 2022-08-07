package com.bbc.km.controller;

import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.service.KitchenMenuItemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/item"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class KitchenMenuItemController extends RESTController<String, KitchenMenuItem> {

    public KitchenMenuItemController(KitchenMenuItemService kitchenMenuItemService) {
        super(kitchenMenuItemService);
    }
}
