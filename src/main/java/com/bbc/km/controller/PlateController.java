package com.bbc.km.controller;

import com.bbc.km.model.Plate;
import com.bbc.km.service.PlateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/plate"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class PlateController extends RESTController<String, Plate> {

    public PlateController(PlateService plateService) {
        super(plateService);
    }

}
