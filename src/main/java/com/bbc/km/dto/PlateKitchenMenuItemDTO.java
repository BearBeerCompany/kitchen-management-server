package com.bbc.km.dto;

import com.bbc.km.model.ItemStatus;
import com.bbc.km.model.KitchenMenuItem;
import com.bbc.km.model.Plate;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PlateKitchenMenuItemDTO implements Serializable {

    private String id;
    private KitchenMenuItem menuItem;
    private Plate plate;
    private ItemStatus status;
    private Integer orderNumber;
    private String tableNumber;
    private String clientName;
    private String notes;
    private String orderNotes;
    private LocalDateTime createdDate;
    private Boolean takeAway;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public KitchenMenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(KitchenMenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public Plate getPlate() {
        return plate;
    }

    public void setPlate(Plate plate) {
        this.plate = plate;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getOrderNotes() {
        return orderNotes;
    }

    public void setOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getTakeAway() {
        return takeAway;
    }

    public void setTakeAway(Boolean takeAway) {
        this.takeAway = takeAway;
    }
}
