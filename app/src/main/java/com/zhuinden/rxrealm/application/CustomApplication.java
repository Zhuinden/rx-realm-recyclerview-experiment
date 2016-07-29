package com.zhuinden.rxrealm.application;

import android.app.Application;

import com.zhuinden.rxrealm.path.cat.CatService;
import com.zhuinden.rxrealm.path.dog.Dog;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.rx.RealmObservableFactory;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by Zhuinden on 2016.07.07..
 */
public class CustomApplication
        extends Application {
    static CustomApplication INSTANCE;

    public RealmConfiguration realmConfiguration;

    public CatService catService;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        realmConfiguration = new RealmConfiguration.Builder(CustomApplication.get()) //
                .deleteRealmIfMigrationNeeded() //
                .initialData(new Realm.Transaction() { //
                    @Override
                    public void execute(Realm realm) {
                        Dog dog;
                        dog = new Dog();
                        dog.setId(1);
                        dog.setName("Woofwoof");
                        realm.insertOrUpdate(dog);
                        dog = new Dog();
                        dog.setId(2);
                        dog.setName("Beetroot");
                        realm.insertOrUpdate(dog);
                        dog = new Dog();
                        dog.setId(3);
                        dog.setName("Ribbon");
                        realm.insertOrUpdate(dog);
                        dog = new Dog();
                        dog.setId(4);
                        dog.setName("Moose");
                        realm.insertOrUpdate(dog);
                        dog = new Dog();
                        dog.setId(5);
                        dog.setName("T-Rex");
                        realm.insertOrUpdate(dog);
                    }
                }) //
                .inMemory() //
                .rxFactory(new RealmObservableFactory()) //
                .build();

        catService = new Retrofit.Builder().addConverterFactory(SimpleXmlConverterFactory.create())
                .baseUrl("http://thecatapi.com/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(CatService.class);
    }

    public static CustomApplication get() {
        return INSTANCE;
    }
}
