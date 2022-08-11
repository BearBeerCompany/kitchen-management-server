package com.bbc.km.websocket;

import com.bbc.km.dto.PlateKitchenMenuItemDTO;
import com.bbc.km.model.PlateKitchenMenuItem;

import java.util.ArrayList;
import java.util.List;

public class PKMINotification {

    private PKMINotificationType type;
    private List<String> ids = new ArrayList<>();
    private PlateKitchenMenuItemDTO plateKitchenMenuItem;

    public PKMINotificationType getType() {
        return type;
    }

    public void setType(PKMINotificationType type) {
        this.type = type;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public PlateKitchenMenuItemDTO getPlateKitchenMenuItem() {
        return plateKitchenMenuItem;
    }

    public void setPlateKitchenMenuItem(PlateKitchenMenuItemDTO plateKitchenMenuItem) {
        this.plateKitchenMenuItem = plateKitchenMenuItem;
    }
}
