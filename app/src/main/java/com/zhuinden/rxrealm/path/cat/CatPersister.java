package com.zhuinden.rxrealm.path.cat;

import android.os.Looper;

import com.zhuinden.rxrealm.util.RealmUtils;

/**
 * Created by Zhuinden on 2016.07.29..
 */
public class CatPersister {
    public void persist(CatsBO catsBO) {
        if(Looper.myLooper() != null) {
            throw new IllegalStateException("Expected to be called in io() scheduler but was called on main thread");
        }
        RealmUtils.executeInTransaction(realm -> {
            Cat defaultCat = new Cat();
            long rank;
            if(realm.where(Cat.class).count() > 0) {
                rank = realm.where(Cat.class).max(Cat.Fields.RANK.getField()).longValue();
            } else {
                rank = 0;
            }
            for(CatBO catBO : catsBO.getCats()) {
                defaultCat.setId(catBO.getId());
                defaultCat.setRank(++rank);
                defaultCat.setSourceUrl(catBO.getSourceUrl());
                defaultCat.setUrl(catBO.getUrl());
                realm.insertOrUpdate(defaultCat);
            }
        });
    }
}
