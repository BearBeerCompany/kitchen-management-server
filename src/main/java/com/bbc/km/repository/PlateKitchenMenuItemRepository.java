package com.bbc.km.repository;

import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.model.PlateKitchenMenuItem;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlateKitchenMenuItemRepository extends MongoRepository<PlateKitchenMenuItem, String> {

    @Aggregation(pipeline = {
        "{'$match':{" +
            "'_id': '?0'" +
            "}}",
        "{'$lookup':{" +
            "'from': 'plate'," +
            "'let': {'searchId': {'$toObjectId': '$plateId'}}," +
            "'pipeline': [{'$match':{'$expr':{'$eq': ['$_id', '$$searchId']}}}]," +
            "'as': 'plate'" +
            "}}",
        "{'$lookup':{" +
            "'from': 'kitchen_menu_item'," +
            "'let': {'searchMenuItemId': {'$toObjectId': '$menuItemId'}}," +
            "'pipeline': [{'$match':{'$expr':{'$eq': ['$_id', '$$searchMenuItemId']}}}]," +
            "'as': 'menuItem'" +
            "}}",
        "{'$project': " +
            "{'status': 1," +
                "'notes': 1," +
                "'clientName': 1," +
                "'tableNumber': 1," +
                "'orderNumber': 1," +
                "'plate': { $arrayElemAt: ['$plate', 0] }" +
                "'menuItem': { $arrayElemAt: ['$menuItem', 0] }" +
            "}}"
    })
    public PlateKitchenMenuItemDTO findPlateKitchenMenuItemDtoById(String id);
}
