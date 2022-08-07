package com.bbc.km.service;

import java.util.List;

public interface CRUDService<ID, DTO> {

    DTO getById(ID id);

    List<DTO> getAll();

    DTO create(DTO dto);

    DTO update(DTO dto);

    void delete(ID id);
}
