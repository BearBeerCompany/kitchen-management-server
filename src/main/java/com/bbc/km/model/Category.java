package com.bbc.km.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("category")
public class Category extends MongoDocument<String> {

    private String name;
    private String description;
    private String color;
    /**
     * Represents id in external (GSG) db
     */
    private Integer externalId;
    private Boolean visible;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
