package com.bbc.km.repository;

import com.bbc.km.model.CategoryMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryMappingRepository extends MongoRepository<CategoryMapping, String> {
}
