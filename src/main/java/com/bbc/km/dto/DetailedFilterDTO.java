package com.bbc.km.dto;

public class DetailedFilterDTO {

    private final String tableNumber;
    private final String clientName;
    private final String itemId;
    private final Integer orderNumber;

    public DetailedFilterDTO(String tableNumber, String clientName, String categoryId, String itemId, Integer orderNumber) {
        this.tableNumber = tableNumber;
        this.clientName = clientName;
        this.itemId = itemId;
        this.orderNumber = orderNumber;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public String getClientName() {
        return clientName;
    }

    public String getItemId() {
        return itemId;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }
}
