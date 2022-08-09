package com.bbc.km.compound;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CompoundController<ID, DTO> {

    ResponseEntity<DTO> getById(ID id);

    ResponseEntity<List<DTO>> getAll();

    ResponseEntity<List<DTO>> createAll(List<DTO> dtoList);

    ResponseEntity<List<DTO>> updateAll(List<DTO> dtoList);

    ResponseEntity<Map<String, Boolean>> deleteAll(List<ID> ids);

}
