package com.bbc.km.repository;

import com.bbc.km.model.KitchenMenuItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitchenMenuItemRepository extends MongoRepository<KitchenMenuItem, String> {

    List<KitchenMenuItem> findByCategoryId(String categoryId);

    List<KitchenMenuItem> findByExternalId(Integer externalId);
}
