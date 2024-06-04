package com.bbc.km.integration;

import com.bbc.km.model.Category;
import com.bbc.km.model.KitchenMenuItem;

import java.util.List;

public class GSGIntegrationResult {

    List<Category> createdCategoryList;
    List<KitchenMenuItem> createdKmiList;

    public List<Category> getCreatedCategoryList() {
        return createdCategoryList;
    }

    public void setCreatedCategoryList(List<Category> createdCategoryList) {
        this.createdCategoryList = createdCategoryList;
    }

    public List<KitchenMenuItem> getCreatedKmiList() {
        return createdKmiList;
    }

    public void setCreatedKmiList(List<KitchenMenuItem> createdKmiList) {
        this.createdKmiList = createdKmiList;
    }
}
