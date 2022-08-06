package com.bbc.km.repository;

import com.bbc.km.model.Plate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlateRepository extends MongoRepository<Plate, String> {
}
