package com.zhuinden.rxrealm.application.injection;

import com.zhuinden.rxrealm.application.CustomApplication;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Zhuinden on 2016.07.29..
 */
@Module
public class RealmModule {
    Realm realm;

    public RealmModule(Realm realm) {
        this.realm = realm;
    }

    @Provides
    public Realm uiThreadRealm() {
        return realm;
    }

    @Provides
    public RealmConfiguration realmConfiguration(CustomApplication customApplication) {
        return customApplication.realmConfiguration;
    }
}
