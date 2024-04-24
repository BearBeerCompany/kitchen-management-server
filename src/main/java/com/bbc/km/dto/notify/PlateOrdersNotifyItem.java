package com.bbc.km.dto.notify;

import java.io.Serializable;

public class PlateOrdersNotifyItem implements Serializable {

    private String id;
    private Integer orderNumber;
    private String tableNumber;
    private String date;
    private String time;
    private String clientName;
    private String orderNotes;
    private Integer quantity;
    private String menuItemName;
    private String menuItemNotes;
    private Integer menuItemExtIndex;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getOrderNotes() {
        return orderNotes;
    }

    public void setOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public String getMenuItemNotes() {
        return menuItemNotes;
    }

    public void setMenuItemNotes(String menuItemNotes) {
        this.menuItemNotes = menuItemNotes;
    }

    public Integer getMenuItemExtIndex() {
        return menuItemExtIndex;
    }

    public void setMenuItemExtIndex(Integer menuItemExtIndex) {
        this.menuItemExtIndex = menuItemExtIndex;
    }
}
