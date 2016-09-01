package com.zhuinden.rxrealm.util;

import com.zhuinden.rxrealm.application.injection.Injector;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Zhuinden on 2016.07.29..
 */
public class RealmUtils {
    static void executeInTransaction(RealmConfiguration realmConfiguration, Realm.Transaction realmTransaction) {
        Realm realm = null;
        try {
            realm = Realm.getInstance(realmConfiguration);
            realm.executeTransaction(realmTransaction);
        } finally {
            if(realm != null) {
                realm.close();
            }
        }
    }

    public static void executeInTransaction(Realm.Transaction realmTransaction) {
        executeInTransaction(Injector.INSTANCE.getComponent().realmConfiguration(), realmTransaction);
    }
}
