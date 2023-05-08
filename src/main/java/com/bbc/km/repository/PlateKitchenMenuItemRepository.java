package com.bbc.km.repository;

import com.bbc.km.dto.DetailedFilterDTO;
import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.model.ItemStatus;
import com.bbc.km.util.JSONOperation;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.bbc.km.repository.PlateKitchenMenuItemJPARepository.MENU_ITEM_LOOKUP;
import static com.bbc.km.repository.PlateKitchenMenuItemJPARepository.PLATE_LOOKUP;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

@Repository
public class PlateKitchenMenuItemRepository {

    private final static ProjectionOperation DTO_PROJECTION = project()
            .and(ArrayOperators.ArrayElemAt.arrayOf("plate").elementAt(0)).as("plate")
            .and(ArrayOperators.ArrayElemAt.arrayOf("menuItem").elementAt(0)).as("menuItem")
            .andInclude("status", "notes", "clientName", "tableNumber", "orderNumber", "createdDate");

    private final MongoTemplate mongoTemplate;

    public PlateKitchenMenuItemRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<PlateKitchenMenuItemDTO> findAll(List<ItemStatus> statuses,
                                                 Integer offset,
                                                 Integer size,
                                                 DetailedFilterDTO query) {

        AggregationResults<PlateKitchenMenuItemDTO> resultsDTO = mongoTemplate.aggregate(Aggregation.newAggregation(
                buildMongoQuery(statuses, query),
                JSONOperation.of(PLATE_LOOKUP),
                JSONOperation.of(MENU_ITEM_LOOKUP),
                Aggregation.skip((long) offset),
                Aggregation.limit(size),
                DTO_PROJECTION
        ), "plate_kitchen_menu_item", PlateKitchenMenuItemDTO.class);

        return resultsDTO.getMappedResults();
    }

    @SuppressWarnings("all")
    public Integer count(List<ItemStatus> statuses,
                         DetailedFilterDTO query) {

        AggregationResults<Document> result = mongoTemplate.aggregate(Aggregation.newAggregation(
                buildMongoQuery(statuses, query),
                JSONOperation.of(PLATE_LOOKUP),
                JSONOperation.of(MENU_ITEM_LOOKUP),
                Aggregation.count().as("total")
        ), "plate_kitchen_menu_item", Document.class);

        return (Integer) result.getUniqueMappedResult().get("total");
    }

    private AggregationOperation buildMongoQuery(List<ItemStatus> statuses,
                                                 DetailedFilterDTO query) {
        Criteria mongoQuery = Criteria.where("status").in(statuses);

        if (query.getTableNumber() != null) {
            mongoQuery.and("tableNumber").is(query.getTableNumber());
        }

        if (query.getClientName() != null) {
            mongoQuery.and("clientName").is(query.getClientName());
        }

        if (query.getItemId() != null) {
            mongoQuery.and("menuItemId").is(query.getItemId());
        }

        if (query.getOrderNumber() != null) {
            mongoQuery.and("orderNumber").is(query.getOrderNumber());
        }

        return Aggregation.match(mongoQuery);
    }
}
