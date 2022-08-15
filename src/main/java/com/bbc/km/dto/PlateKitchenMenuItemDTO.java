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
    private Integer tableNumber;
    private String clientName;
    private String notes;
    private LocalDateTime createdDate;

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

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(Integer tableNumber) {
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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
