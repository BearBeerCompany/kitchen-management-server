package com.bbc.km.model;

import org.springframework.data.annotation.Id;

public abstract class MongoDocument<ID> extends Auditable {

    @Id
    private ID id;

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }
}
