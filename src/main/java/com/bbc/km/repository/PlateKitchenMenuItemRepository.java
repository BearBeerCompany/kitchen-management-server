package com.bbc.km.repository;

import com.bbc.km.model.PlateKitchenMenuItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlateKitchenMenuItemRepository extends MongoRepository<PlateKitchenMenuItem, String> {
}
