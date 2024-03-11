package com.bbc.km.compound;

import com.bbc.km.dto.DetailedFilterDTO;
import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.exception.ObjectNotFoundException;
import com.bbc.km.model.*;
import com.bbc.km.repository.PlateKitchenMenuItemJPARepository;
import com.bbc.km.repository.PlateKitchenMenuItemRepository;
import com.bbc.km.service.CRUDService;
import com.bbc.km.service.PlateService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PlateKitchenMenuItemCompound {

    private final CRUDService<String, PlateKitchenMenuItem> pkmiService;
    private final CRUDService<String, KitchenMenuItem> kmiService;
    private final PlateService plateService;
    private final PlateKitchenMenuItemJPARepository plateKitchenMenuItemJPARepository;
    private final PlateKitchenMenuItemRepository plateKitchenMenuItemRepository;

    public PlateKitchenMenuItemCompound(
            CRUDService<String, PlateKitchenMenuItem> pkmiService,
            CRUDService<String, KitchenMenuItem> kmiService,
            PlateService plateService,
            PlateKitchenMenuItemJPARepository plateKitchenMenuItemJPARepository,
            PlateKitchenMenuItemRepository plateKitchenMenuItemRepository) {
        this.pkmiService = pkmiService;
        this.kmiService = kmiService;
        this.plateService = plateService;
        this.plateKitchenMenuItemJPARepository = plateKitchenMenuItemJPARepository;
        this.plateKitchenMenuItemRepository = plateKitchenMenuItemRepository;
    }

    public PlateKitchenMenuItemDTO getById(String id) {
        PlateKitchenMenuItem pkmiDoc = pkmiService.getById(id);
        PlateKitchenMenuItemDTO dto = doc2Dto(pkmiDoc);
        return dto;
    }

    public List<PlateKitchenMenuItemDTO> getByIds(List<String> ids) {
        List<PlateKitchenMenuItemDTO> dtoList = pkmiService.getAll()
                .stream().filter(doc -> ids.contains(doc.getId())).map(doc -> {
                    PlateKitchenMenuItemDTO dto = doc2Dto(doc);
                    return dto;
                }).collect(Collectors.toList());

        return dtoList;
    }

    public PageResponse<PlateKitchenMenuItemDTO> getAll(List<ItemStatus> statuses,
                                                        Integer offset,
                                                        Integer size,
                                                        DetailedFilterDTO query) {
        statuses = statuses != null ? statuses : List.of(ItemStatus.values());
        List<PlateKitchenMenuItemDTO> elements = plateKitchenMenuItemRepository.findAll(statuses, offset, size, query);
        return PageResponse.of(elements, offset, size, plateKitchenMenuItemRepository.count(statuses, query));
    }

    public PlateKitchenMenuItemDTO create(PlateKitchenMenuItemDTO dto) {
        PlateKitchenMenuItem doc = dto2Doc(dto);
        PlateKitchenMenuItem newDoc = pkmiService.create(doc);
        return doc2Dto(newDoc);
    }

    public List<PlateKitchenMenuItemDTO> createAll(List<PlateKitchenMenuItemDTO> dtoList) {
        List<PlateKitchenMenuItemDTO> newDtoList = new ArrayList<>();
        dtoList.forEach(dto -> {
            PlateKitchenMenuItem doc = dto2Doc(dto);
            PlateKitchenMenuItem newDoc = pkmiService.create(doc);
            newDtoList.add(doc2Dto(newDoc));
        });

        return newDtoList;
    }

    public PlateKitchenMenuItemDTO update(PlateKitchenMenuItemDTO dto) {
        PlateKitchenMenuItem doc = dto2Doc(dto);
        PlateKitchenMenuItem newDoc = pkmiService.update(doc);
        plateService.incrementCounterById(dto.getPlate().getId());
        return doc2Dto(newDoc);
    }

    public List<PlateKitchenMenuItemDTO> updateAll(List<PlateKitchenMenuItemDTO> dtoList) {
        List<PlateKitchenMenuItemDTO> updatedDtoList = new ArrayList<>();
        dtoList.forEach(dto -> {
            PlateKitchenMenuItem doc = dto2Doc(dto);
            PlateKitchenMenuItem updatedDoc = pkmiService.update(doc);
            updatedDtoList.add(doc2Dto(updatedDoc));
        });

        return updatedDtoList;
    }

    public PlateKitchenMenuItemDTO delete(String id) {
        PlateKitchenMenuItemDTO result = null;
        try {
            PlateKitchenMenuItem item = pkmiService.delete(id);
            result = doc2Dto(item);
        } catch (ObjectNotFoundException ex) {
            // noop
        }
        return result;
    }

    public Map<String, Boolean> deleteAll(List<String> ids) {
        Map<String, Boolean> resultMap = new HashMap<>();
        ids.forEach(id -> {
            try {
                pkmiService.delete(id);
                resultMap.put(id, true);
            } catch (ObjectNotFoundException ex) {
                resultMap.put(id, false);
            }
        });
        return resultMap;
    }

    private PlateKitchenMenuItemDTO doc2Dto(PlateKitchenMenuItem doc) {
        PlateKitchenMenuItemDTO dto = new PlateKitchenMenuItemDTO();
        String menuItemId = doc.getMenuItemId();
        String plateId = doc.getPlateId();

        // retrieve menuItem data
        KitchenMenuItem kmiDoc = kmiService.getById(menuItemId);
        // retrieve plate data
        Plate plate = (plateId != null) ? plateService.getById(plateId) : null;

        dto.setId(doc.getId());
        dto.setMenuItem(kmiDoc);
        dto.setPlate(plate);
        dto.setOrderNumber(doc.getOrderNumber());
        dto.setClientName(doc.getClientName());
        dto.setStatus(doc.getStatus());
        dto.setTableNumber(doc.getTableNumber());
        dto.setNotes(doc.getNotes());
        dto.setCreatedDate(doc.getCreatedDate());
        dto.setTakeAway(doc.getTakeAway());
        return dto;
    }

    private PlateKitchenMenuItem dto2Doc(PlateKitchenMenuItemDTO dto) {
        PlateKitchenMenuItem doc = new PlateKitchenMenuItem();
        doc.setId(dto.getId());
        doc.setMenuItemId(dto.getMenuItem().getId());
        if (dto.getPlate() != null) {
            doc.setPlateId(dto.getPlate().getId());
        }
        doc.setOrderNumber(dto.getOrderNumber());
        doc.setTableNumber(dto.getTableNumber());
        doc.setStatus(dto.getStatus());
        doc.setClientName(dto.getClientName());
        doc.setNotes(dto.getNotes());
        if (dto.getCreatedDate() != null) {
            doc.setCreatedDate(dto.getCreatedDate());
        }
        doc.setTakeAway(dto.getTakeAway());
        return doc;
    }

}
