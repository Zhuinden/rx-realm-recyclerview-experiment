package com.zhuinden.rxrealm.application.injection;

import com.zhuinden.rxrealm.application.CustomApplication;
import com.zhuinden.rxrealm.path.cat.CatModule;
import com.zhuinden.rxrealm.path.cat.CatPersister;
import com.zhuinden.rxrealm.path.cat.CatView;
import com.zhuinden.rxrealm.path.dog.DogView;

import javax.inject.Singleton;

import dagger.Component;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Zhuinden on 2016.07.29..
 */
@Singleton
@Component(modules = {AppContextModule.class, RealmModule.class, CatModule.class})
public interface ApplicationComponent {
    CustomApplication customApplication();

    Realm realm();

    RealmConfiguration realmConfiguration();

    CatPersister catPersister();

    void inject(DogView dogView);

    void inject(CatView catView);
}
