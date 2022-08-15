package com.bbc.km.repository;

import com.bbc.km.model.Stats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends MongoRepository<Stats, String> {

    @Query("{'createdDate' : { $gte: ?0, $lte: ?1 } }")
    List<Stats> findByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
