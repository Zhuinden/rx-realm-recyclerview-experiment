package com.zhuinden.rxrealm.application.injection;

import com.zhuinden.rxrealm.path.cat.CatService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by Zhuinden on 2016.08.12..
 */
@Module
public class ServiceModule {
    @Provides
    @Singleton
    public CatService catService() {
        return new Retrofit.Builder().addConverterFactory(SimpleXmlConverterFactory.create())
                .baseUrl("http://thecatapi.com/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(CatService.class);
    }
}
