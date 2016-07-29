package com.zhuinden.rxrealm.path.cat;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Zhuinden on 2016.07.29..
 */
@Module
public class CatModule {
    @Provides
    @Singleton
    public CatPersister catPersister() {
        return new CatPersister();
    }
}
