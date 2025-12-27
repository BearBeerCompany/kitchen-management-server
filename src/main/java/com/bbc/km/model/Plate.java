package com.bbc.km.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;
import java.util.List;

@Document("plate")
public class Plate extends MongoDocument<String> {

    private String name;
    private String description;
    private String color;
    private String manager;
    private List<Integer> slot = List.of(0,0);
    private Boolean enabled = true;
    /**
     * List of categories associated with current plate for automatic order insertion.
     *
     * @see <a href="https://github.com/BearBeerCompany/kitchen-management-server/issues/56">Feature Request #56</a>
     */
    private List<String> categories = new LinkedList<>();

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

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public List<Integer> getSlot() {
        return slot;
    }

    public void setSlot(List<Integer> slot) {
        this.slot = slot;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}