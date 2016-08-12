package com.zhuinden.rxrealm.application.injection;

import com.zhuinden.rxrealm.path.cat.CatModule;
import com.zhuinden.rxrealm.path.cat.CatView;
import com.zhuinden.rxrealm.path.dog.DogView;

import javax.inject.Singleton;

import dagger.Component;
import io.realm.RealmConfiguration;

/**
 * Created by Zhuinden on 2016.07.29..
 */
@Singleton
@Component(modules = {AppContextModule.class, RealmModule.class, CatModule.class, ServiceModule.class})
public interface ApplicationComponent {
    RealmConfiguration realmConfiguration();

    void inject(DogView dogView);

    void inject(CatView catView);
}
