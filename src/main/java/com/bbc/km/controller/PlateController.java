package com.bbc.km.controller;

import com.bbc.km.model.Plate;
import com.bbc.km.service.PlateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/plate"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class PlateController {

    private final PlateService plateService;

    public PlateController(PlateService plateService) {
        this.plateService = plateService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plate> getById(@PathVariable String id) {
        return ResponseEntity.ok(plateService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Plate>> getAll() {
        return ResponseEntity.ok(plateService.getAll());
    }

    @PostMapping
    public ResponseEntity<Plate> create(@RequestBody Plate plate) {
        return ResponseEntity.ok(plateService.create(plate));
    }

    @PutMapping
    public ResponseEntity<Plate> update(@RequestBody Plate plate) {
        return ResponseEntity.ok(plateService.update(plate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        plateService.delete(id);
        return ResponseEntity.ok().build();
    }
}
