package com.bbc.km.integration;

import com.bbc.km.exception.ObjectNotFoundException;
import com.bbc.km.jpa.entity.Articolo;
import com.bbc.km.jpa.entity.Tipologia;
import com.bbc.km.jpa.repository.TipologiaRepository;
import com.bbc.km.model.Category;
import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.model.Plate;
import com.bbc.km.repository.PlateRepository;
import com.bbc.km.service.CategoryMappingService;
import com.bbc.km.service.CategoryService;
import com.bbc.km.service.KitchenMenuItemService;
import com.bbc.km.service.PlateKitchenMenuItemService;
import com.bbc.km.service.PlateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GSGIntegrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GSGIntegrationService.class);

    private PlateKitchenMenuItemService pkmiService;
    private CategoryService categoryService;
    private KitchenMenuItemService kmiService;
    private PlateRepository plateRepository;
    private TipologiaRepository tipologiaRepository;
    private CategoryMappingService categoryMappingService;

    public GSGIntegrationService(
            PlateKitchenMenuItemService pkmiService,
            CategoryService categoryService,
            KitchenMenuItemService kmiService,
            PlateRepository plateRepository,
            TipologiaRepository tipologiaRepository,
            CategoryMappingService categoryMappingService) {
        this.pkmiService = pkmiService;
        this.categoryService = categoryService;
        this.kmiService = kmiService;
        this.plateRepository = plateRepository;
        this.tipologiaRepository = tipologiaRepository;
        this.categoryMappingService = categoryMappingService;
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

            // lookup from GSG Tipologia.id to the freshly created Mongo Category id
            Map<Integer, String> externalCategoryToMongoId = new HashMap<>();
            for (Category category : createdCategories) {
                externalCategoryToMongoId.put(category.getExternalId(), category.getId());
            }
            // category overrides (item-level takes precedence over category-level), keyed by stable GSG external ids
            Map<Integer, Integer> itemOverrides = categoryMappingService.getItemOverrides();
            Map<Integer, Integer> categoryOverrides = categoryMappingService.getCategoryOverrides();

            // retrieve menu items for each created category
            List<KitchenMenuItem> menuItems = new ArrayList<>();
            createdCategories.forEach(category -> {
                int externalId = category.getExternalId();
                Optional<Tipologia> tipologiaOpt = tipologiaList.stream().filter(tip -> tip.getId() == externalId).findFirst();
                if (tipologiaOpt.isPresent()) {
                    Set<Articolo> articoloSet = tipologiaOpt.get().getArticoloSet();
                    menuItems.addAll(articoloSet.stream().map(articolo -> {
                        String resolvedCategoryId = resolveCategoryId(
                                articolo.getId(),
                                externalId,
                                category.getId(),
                                externalCategoryToMongoId,
                                itemOverrides,
                                categoryOverrides);
                        return GSGIntegrationService.map(articolo, resolvedCategoryId);
                    }).collect(Collectors.toList()));
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

    /**
     * Resolves the Mongo category id an item must be assigned to, honoring category overrides.
     * Item-level overrides ({@code GSG Articolo.id}) take precedence over category-level ones
     * ({@code GSG Tipologia.id}). When no override applies, or the override points to a category that
     * was not imported (e.g. a non-visible Tipologia), the item keeps its original GSG category.
     *
     * @param articoloId               GSG {@code Articolo.id} of the item
     * @param sourceExternalCategoryId GSG {@code Tipologia.id} the item belongs to in GSG
     * @param defaultCategoryId        Mongo category id matching the item's GSG category (fallback)
     * @param externalCategoryToMongoId lookup {@code GSG Tipologia.id -> Mongo Category id}
     * @param itemOverrides            {@code GSG Articolo.id -> target GSG Tipologia.id}
     * @param categoryOverrides        {@code source GSG Tipologia.id -> target GSG Tipologia.id}
     */
    private String resolveCategoryId(Integer articoloId,
                                     Integer sourceExternalCategoryId,
                                     String defaultCategoryId,
                                     Map<Integer, String> externalCategoryToMongoId,
                                     Map<Integer, Integer> itemOverrides,
                                     Map<Integer, Integer> categoryOverrides) {
        Integer targetExternalCategoryId = itemOverrides.get(articoloId);
        if (targetExternalCategoryId == null) {
            targetExternalCategoryId = categoryOverrides.get(sourceExternalCategoryId);
        }

        if (targetExternalCategoryId == null) {
            return defaultCategoryId;
        }

        String targetCategoryId = externalCategoryToMongoId.get(targetExternalCategoryId);
        if (targetCategoryId == null) {
            LOGGER.warn("GSGIntegrationService::resolveCategoryId - override target category {} not found among imported categories for articolo {}, falling back to original GSG category {}",
                    targetExternalCategoryId, articoloId, sourceExternalCategoryId);
            return defaultCategoryId;
        }

        LOGGER.info("GSGIntegrationService::resolveCategoryId - articolo {} remapped from GSG category {} to category {}",
                articoloId, sourceExternalCategoryId, targetExternalCategoryId);
        return targetCategoryId;
    }
}
