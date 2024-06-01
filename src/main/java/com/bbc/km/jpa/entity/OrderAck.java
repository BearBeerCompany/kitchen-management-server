package com.bbc.km.jpa.entity;

import javax.persistence.*;

@Entity
@Table(name = "orders_ack")
public class OrderAck {

    @Id
    private int id;
    @Column(name = "order_number")
    private int orderNumber;
    @Column(name = "table_number")
    private String tableNumber;
    @Column(name = "insert_date")
    private String insertDate;
    @Column(name = "insert_time")
    private String insertTime;
    @Column(name = "client_name")
    private String clientName;
    @Column(name = "take_away")
    private boolean takeAway;
    @Column(name = "order_notes")
    private String orderNotes;
    private int quantity;
    @Column(name = "menu_item_id")
    private int menuItemId;
    @Column(name = "menu_item_name")
    private String menuItemName;
    @Column(name = "menu_item_notes")
    private String menuItemNotes;
    @Column(name = "category_id")
    private int categoryId;
    @Column(name = "category_name")
    private String categoryName;
    private boolean ack;

    // Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(String insertDate) {
        this.insertDate = insertDate;
    }

    public String getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(String insertTime) {
        this.insertTime = insertTime;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public boolean isTakeAway() {
        return takeAway;
    }

    public void setTakeAway(boolean takeAway) {
        this.takeAway = takeAway;
    }

    public String getOrderNotes() {
        return orderNotes;
    }

    public void setOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(int menuItemId) {
        this.menuItemId = menuItemId;
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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }
}

