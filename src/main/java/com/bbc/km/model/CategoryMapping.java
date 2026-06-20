package com.bbc.km.model;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Override of the category an item belongs to in this application with respect to the category it has
 * in the external Gestione Stand Gastronomico (GSG) system.
 * <p>
 * GSG classifies items under a {@code Tipologia} (sales/menu taxonomy), but the kitchen may want to
 * group/route an item differently (e.g. the burger "Elton John" is sold as <i>Panini</i> in GSG but is
 * prepared at the <i>Piatti Unici</i> station). These mappings are applied during the GSG import
 * ({@code GSGIntegrationService.init()}) and, being stored in a dedicated collection, survive re-imports.
 * <p>
 * All references use stable GSG external ids (not Mongo {@code _id}s, which are regenerated on every import):
 * <ul>
 *     <li>{@link #externalItemId} - GSG {@code Articolo.id}, for an <b>item-level</b> override</li>
 *     <li>{@link #sourceExternalCategoryId} - GSG {@code Tipologia.id}, for a <b>category-level</b> override</li>
 *     <li>{@link #targetExternalCategoryId} - GSG {@code Tipologia.id} of the destination category</li>
 * </ul>
 * Exactly one of {@code externalItemId} / {@code sourceExternalCategoryId} must be set. Item-level
 * overrides take precedence over category-level ones.
 */
@Document("category_mapping")
public class CategoryMapping extends MongoDocument<String> {

    /**
     * GSG {@code Articolo.id} this override applies to (item-level). Mutually exclusive with
     * {@link #sourceExternalCategoryId}.
     */
    private Integer externalItemId;

    /**
     * GSG {@code Tipologia.id} this override applies to (category-level). Mutually exclusive with
     * {@link #externalItemId}.
     */
    private Integer sourceExternalCategoryId;

    /**
     * GSG {@code Tipologia.id} of the category the item(s) should be assigned to in this application.
     */
    private Integer targetExternalCategoryId;

    /**
     * Optional human-readable note (e.g. "Elton John -> Piatti Unici").
     */
    private String description;

    public Integer getExternalItemId() {
        return externalItemId;
    }

    public void setExternalItemId(Integer externalItemId) {
        this.externalItemId = externalItemId;
    }

    public Integer getSourceExternalCategoryId() {
        return sourceExternalCategoryId;
    }

    public void setSourceExternalCategoryId(Integer sourceExternalCategoryId) {
        this.sourceExternalCategoryId = sourceExternalCategoryId;
    }

    public Integer getTargetExternalCategoryId() {
        return targetExternalCategoryId;
    }

    public void setTargetExternalCategoryId(Integer targetExternalCategoryId) {
        this.targetExternalCategoryId = targetExternalCategoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
