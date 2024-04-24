package com.bbc.km.dto.notify;

import java.io.Serializable;

public class PlateOrdersNotifyDTO implements Serializable {

    private DbTriggerOperation operation;
    private PlateOrdersNotifyItem item;

    public DbTriggerOperation getOperation() {
        return operation;
    }

    public void setOperation(DbTriggerOperation operation) {
        this.operation = operation;
    }

    public PlateOrdersNotifyItem getItem() {
        return item;
    }

    public void setItem(PlateOrdersNotifyItem item) {
        this.item = item;
    }
}
