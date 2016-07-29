package com.zhuinden.rxrealm.application.injection;

import com.zhuinden.rxrealm.application.CustomApplication;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Zhuinden on 2016.07.29..
 */
@Module
public class AppContextModule {
    @Provides
    public CustomApplication application() {
        return CustomApplication.get();
    }
}
