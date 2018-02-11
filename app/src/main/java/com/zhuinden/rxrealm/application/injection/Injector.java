package com.zhuinden.rxrealm.application.injection;

import io.realm.Realm;

/**
 * Created by Zhuinden on 2016.07.29..
 */
public enum Injector {
    INSTANCE;

    private ApplicationComponent applicationComponent;

    Injector() {
    }

    public void initializeComponent(Realm realm) {
        RealmModule realmModule = new RealmModule(realm);
        applicationComponent = DaggerApplicationComponent.builder().realmModule(realmModule).build();
    }

    public static ApplicationComponent get() {
        return INSTANCE.applicationComponent;
    }
}