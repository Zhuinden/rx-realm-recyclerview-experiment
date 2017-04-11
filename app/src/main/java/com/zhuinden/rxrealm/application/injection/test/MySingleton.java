package com.zhuinden.rxrealm.application.injection.test;

import com.zhuinden.rxrealm.application.CustomApplication;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Zhuinden on 2016.08.23..
 */
@Singleton
public class MySingleton {
    CustomApplication context;
    MyOtherSingleton myOtherSingleton;

    @Inject
    public MySingleton(CustomApplication customApplication, MyOtherSingleton myOtherSingleton) {
        this.context = customApplication;
        this.myOtherSingleton = myOtherSingleton;
    }

    public CustomApplication getContext() {
        return context;
    }

    public MyOtherSingleton getMyOtherSingleton() {
        return myOtherSingleton;
    }
}