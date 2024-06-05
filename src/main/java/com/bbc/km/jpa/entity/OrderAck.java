package com.bbc.km.jpa.entity;

import javax.persistence.*;

@Entity
@Table(name = "orders_ack")
public class OrderAck {

    @Id
    private Integer id;
    @Column(name = "order_number")
    private Integer orderNumber;
    @Column(name = "table_number")
    private String tableNumber;
    @Column(name = "insert_date")
    private String insertDate;
    @Column(name = "insert_time")
    private String insertTime;
    @Column(name = "client_name")
    private String clientName;
    @Column(name = "take_away")
    private Boolean takeAway;
    @Column(name = "order_notes")
    private String orderNotes;
    private Integer quantity;
    @Column(name = "menu_item_id")
    private Integer menuItemId;
    @Column(name = "menu_item_name")
    private String menuItemName;
    @Column(name = "menu_item_notes")
    private String menuItemNotes;
    @Column(name = "category_id")
    private Integer categoryId;
    @Column(name = "category_name")
    private String categoryName;
    private Boolean ack;

    // Getters and setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Boolean getTakeAway() {
        return takeAway;
    }

    public void setTakeAway(Boolean takeAway) {
        this.takeAway = takeAway;
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

    public Integer getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Integer menuItemId) {
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

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Boolean getAck() {
        return ack;
    }

    public void setAck(Boolean ack) {
        this.ack = ack;
    }
}

