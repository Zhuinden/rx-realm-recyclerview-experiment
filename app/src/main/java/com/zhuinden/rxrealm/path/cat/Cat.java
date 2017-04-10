package com.zhuinden.rxrealm.path.cat;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Zhuinden on 2016.07.29..
 */
public class Cat
        extends RealmObject {
    @PrimaryKey
    private String id;

    private String url;

    private String sourceUrl;

    private long rank;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }
}
