package com.bbc.km.repository;

import com.bbc.km.model.Plate;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlateRepository extends MongoRepository<Plate, String> {

    @Query(
        "{ " +
        "  'enabled': true, " +
        "  'categories': ?0 " +
        "}"
    )
    List<Plate> findCandidatePlates(String categoryId, Sort sort);

    @Query(
        "{ " +
        "  'enabled': true, " +
        "  'categories': ?0, " +
        "  $expr: {" +
        "    $lt: [" +
        "      { $arrayElemAt: ['$slot', 0] }, " +
        "      { $arrayElemAt: ['$slot', 1] } " +
        "    ] " +
        "  } " +
        "}"
    )
    List<Plate> findFreePlatesByCategory(String categoryId, Sort sort);

}
