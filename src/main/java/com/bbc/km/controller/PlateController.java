package com.bbc.km.controller;

import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.model.Plate;
import com.bbc.km.model.PlateKitchenMenuItem;
import com.bbc.km.repository.PlateKitchenMenuItemRepository;
import com.bbc.km.service.PlateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/plate"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class PlateController extends RESTController<String, Plate> {

    private final PlateKitchenMenuItemRepository plateKitchenMenuItemRepository;

    public PlateController(PlateService plateService,
                           PlateKitchenMenuItemRepository plateKitchenMenuItemRepository) {
        super(plateService);
        this.plateKitchenMenuItemRepository = plateKitchenMenuItemRepository;
    }


    @GetMapping("/status/{id}")
    public ResponseEntity<List<PlateKitchenMenuItemDTO>> getPlateStatus(@PathVariable("id") String id) {
        return ResponseEntity.ok(plateKitchenMenuItemRepository.findByPlateId(id));
    }
}
