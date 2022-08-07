package com.bbc.km.controller;

import com.bbc.km.model.PlateKitchenMenuItem;
import com.bbc.km.service.PlateKitchenMenuItemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/plate-item"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class PlateKitchenMenuItemController extends RESTController<String, PlateKitchenMenuItem> {

    protected PlateKitchenMenuItemController(PlateKitchenMenuItemService service) {
        super(service);
    }
}
