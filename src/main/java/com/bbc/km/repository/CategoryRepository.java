package com.bbc.km.repository;

import com.bbc.km.model.Category;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    @Query(value = "{ '_id': { $in: ?0 } }", fields = "{ '_id': 1 }")
    List<CategoryIdProjection> findIdsByIdIn(List<String> ids);

    interface CategoryIdProjection {
        @Field("_id")
        String getId();
    }
}
