package com.bbc.km.controller;

import com.bbc.km.compound.CompoundController;
import com.bbc.km.compound.PlateKitchenMenuItemCompound;
import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/plate-item"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class PlateKitchenMenuItemController implements CompoundController<String, PlateKitchenMenuItemDTO> {

    private PlateKitchenMenuItemCompound compound;

    public PlateKitchenMenuItemController(PlateKitchenMenuItemCompound compound) {
        this.compound = compound;
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<PlateKitchenMenuItemDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(compound.getById(id));
    }

    @GetMapping
    @Override
    public ResponseEntity<List<PlateKitchenMenuItemDTO>> getAll() {
        return ResponseEntity.ok(compound.getAll());
    }

    @PostMapping
    @Override
    public ResponseEntity<List<PlateKitchenMenuItemDTO>> create(@RequestBody List<PlateKitchenMenuItemDTO> pkmiList) {
        return ResponseEntity.ok(compound.create(pkmiList));
    }

    @PutMapping
    @Override
    public ResponseEntity<List<PlateKitchenMenuItemDTO>> update(@RequestBody List<PlateKitchenMenuItemDTO> pkmiList) {
        return ResponseEntity.ok(compound.update(pkmiList));
    }

    @DeleteMapping
    @Override
    public ResponseEntity<Map<String, Boolean>> delete(@RequestBody List<String> ids) {
        return ResponseEntity.ok(compound.delete(ids));
    }
}
