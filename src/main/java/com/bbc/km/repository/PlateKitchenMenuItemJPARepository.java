package com.bbc.km.repository;

import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.PlateKitchenMenuItem;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlateKitchenMenuItemJPARepository extends MongoRepository<PlateKitchenMenuItem, String> {

    String PLATE_LOOKUP = "{'$lookup':{" +
            "'from': 'plate'," +
            "'let': {'searchId': {'$toObjectId': '$plateId'}}," +
            "'pipeline': [{'$match':{'$expr':{'$eq': ['$_id', '$$searchId']}}}]," +
            "'as': 'plate'" +
            "}}";

    String MENU_ITEM_LOOKUP = "{'$lookup':{" +
            "'from': 'kitchen_menu_item'," +
            "'let': {'searchMenuItemId': {'$toObjectId': '$menuItemId'}}," +
            "'pipeline': [{'$match':{'$expr':{'$eq': ['$_id', '$$searchMenuItemId']}}}]," +
            "'as': 'menuItem'" +
            "}}";

    String PKMI_DTO_PROJECTION = "{'$project': " +
            "{'status': 1," +
            "'notes': 1," +
            "'orderNotes': 1," +
            "'takeAway': 1," +
            "'clientName': 1," +
            "'tableNumber': 1," +
            "'orderNumber': 1," +
            "'createdDate': 1," +
            "'menuItem': { $arrayElemAt: ['$menuItem', 0] }" +
            "}}";

    String CREATION_DATE_ASC_SORT = "{'$sort': {'createdDate': 1} }";

    @Aggregation(pipeline = {
            "{'$match':{" +
                    "'$and':[" +
                    "{'$or': [" +
                    "{'status': 'TODO'}" +
                    "{'status': 'PROGRESS'}" +
                    "]}" +
                    "{'plateId': '?0'}" +
                    "]" +
                    "}}",
            CREATION_DATE_ASC_SORT,
            MENU_ITEM_LOOKUP,
            PKMI_DTO_PROJECTION
    })
    List<PlateKitchenMenuItemDTO> findByPlateId(String id);

    @Aggregation(pipeline = {
            "{'$match':{" +
                    "'$and':[" +
                    "{'$or': [" +
                    "{'status': 'TODO'}" +
                    "{'status': 'PROGRESS'}" +
                    "]}" +
                    "{'plateId': null}" +
                    "]" +
                    "}}",
            CREATION_DATE_ASC_SORT,
            MENU_ITEM_LOOKUP,
            PKMI_DTO_PROJECTION
    })
    List<PlateKitchenMenuItemDTO> findByPlateIdNull();
}
