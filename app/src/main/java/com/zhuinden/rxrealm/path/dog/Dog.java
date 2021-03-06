package com.zhuinden.rxrealm.path.dog;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Zhuinden on 2016.07.07..
 */
public class Dog extends RealmObject {
    @PrimaryKey
    private long id;

    @Index
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
