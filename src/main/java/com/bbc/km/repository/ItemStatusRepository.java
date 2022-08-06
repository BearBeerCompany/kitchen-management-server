package com.bbc.km.repository;

import com.bbc.km.model.ItemStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemStatusRepository extends MongoRepository<ItemStatus, String> {
}
