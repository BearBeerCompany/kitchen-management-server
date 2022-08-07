package com.bbc.km.controller;

import com.bbc.km.model.MongoDocument;
import com.bbc.km.service.CRUDService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class RESTController<ID, DTO extends MongoDocument<ID>> {

    protected final CRUDService<ID, DTO> service;

    protected RESTController(CRUDService<ID, DTO> service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DTO> getById(@PathVariable ID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<DTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<DTO> create(@RequestBody DTO plate) {
        return ResponseEntity.ok(service.create(plate));
    }

    @PutMapping
    public ResponseEntity<DTO> update(@RequestBody DTO plate) {
        return ResponseEntity.ok(service.update(plate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
