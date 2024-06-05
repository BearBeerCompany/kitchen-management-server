package com.bbc.km.integration;

import com.bbc.km.exception.ObjectNotFoundException;
import com.bbc.km.jpa.entity.Articolo;
import com.bbc.km.jpa.entity.Tipologia;
import com.bbc.km.jpa.repository.TipologiaRepository;
import com.bbc.km.model.Category;
import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.model.Plate;
import com.bbc.km.repository.PlateRepository;
import com.bbc.km.service.CategoryService;
import com.bbc.km.service.KitchenMenuItemService;
import com.bbc.km.service.PlateKitchenMenuItemService;
import com.bbc.km.service.PlateService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GSGIntegrationService {

    private PlateKitchenMenuItemService pkmiService;
    private CategoryService categoryService;
    private KitchenMenuItemService kmiService;
    private PlateRepository plateRepository;
    private TipologiaRepository tipologiaRepository;

    public GSGIntegrationService(
            PlateKitchenMenuItemService pkmiService,
            CategoryService categoryService,
            KitchenMenuItemService kmiService,
            PlateRepository plateRepository,
            TipologiaRepository tipologiaRepository) {
        this.pkmiService = pkmiService;
        this.categoryService = categoryService;
        this.kmiService = kmiService;
        this.plateRepository = plateRepository;
        this.tipologiaRepository = tipologiaRepository;
    }

    public GSGIntegrationResult init() {
        // clean orders
        pkmiService.deleteAll();
        // clean categories and menu items
        categoryService.deleteAll();
        // retrieve plates and clean slots
        List<Plate> plates = plateRepository.findAll();
        for (Plate plate : plates) {
            Integer plateOrders = plate.getSlot().get(0);
            if (plateOrders > 0) {
                plate.getSlot().set(0, 0);
                plateRepository.save(plate);
            }
        }
        // retrieve categories and menu items from GSG
        List<Tipologia> tipologiaList = this.tipologiaRepository.findAll().stream().filter(Tipologia::getVisibile).collect(Collectors.toList());
        List<Category> createdCategories = new ArrayList<>();
        List<KitchenMenuItem> kmiList = new ArrayList<>();
        if (!tipologiaList.isEmpty()) {
            List<Category> categories = tipologiaList.stream().map(GSGIntegrationService::map).collect(Collectors.toList());
            createdCategories = categoryService.createAll(categories);

            // retrieve menu items for each created category
            List<KitchenMenuItem> menuItems = new ArrayList<>();
            createdCategories.forEach(category -> {
                int externalId = category.getExternalId();
                Optional<Tipologia> tipologiaOpt = tipologiaList.stream().filter(tip -> tip.getId() == externalId).findFirst();
                if (tipologiaOpt.isPresent()) {
                    Set<Articolo> articoloSet = tipologiaOpt.get().getArticoloSet();
                    menuItems.addAll(articoloSet.stream().map(articolo -> GSGIntegrationService.map(articolo, category.getId())).collect(Collectors.toList()));
                } else {
                    throw new ObjectNotFoundException(externalId);
                }
            });
            if (!menuItems.isEmpty()) {
                kmiList = kmiService.createAll(menuItems);
            }
        }
        GSGIntegrationResult result = new GSGIntegrationResult();
        result.setCreatedCategoryList(createdCategories);
        result.setCreatedKmiList(kmiList);
        return result;
    }

    private static Category map(Tipologia tipologia) {
        Category category = new Category();
        category.setExternalId(tipologia.getId());
        category.setName(tipologia.getDescrizione());
        category.setDescription(tipologia.getDescrizione());
        category.setVisible(tipologia.getVisibile());
        category.setColor(String.valueOf(tipologia.getSfondo())); // todo capire meglio conversione
        return category;
    }

    private static KitchenMenuItem map(Articolo articolo, String categoryId) {
        KitchenMenuItem menuItem = new KitchenMenuItem();
        menuItem.setExternalId(articolo.getId());
        menuItem.setName(articolo.getDescrizioneBreve());
        menuItem.setDescription(articolo.getDescrizione());
        menuItem.setCategoryId(categoryId);
        return menuItem;
    }
}
