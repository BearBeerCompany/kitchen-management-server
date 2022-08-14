package com.bbc.km.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document("stats")
public class Stats extends MongoDocument<String> {

    private Integer count;
    private Map<ItemStatus, Integer> statusCount = new HashMap<>();

    public Stats(Integer count) {
        this.count = count;
        for (ItemStatus i : ItemStatus.values())
            this.statusCount.put(i, 0);
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Map<ItemStatus, Integer> getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(Map<ItemStatus, Integer> statusCount) {
        this.statusCount = statusCount;
    }

    public void addCount() {
        this.count+=1;
    }
}
