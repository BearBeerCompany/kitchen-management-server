package com.bbc.km.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("kitchen_menu_item")
public class KitchenMenuItem extends MongoDocument<String> {

    private String name;
    private String description;
    private String categoryId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
