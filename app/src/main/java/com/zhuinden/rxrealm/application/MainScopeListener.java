package com.zhuinden.rxrealm.application;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.zhuinden.rxrealm.application.injection.Injector;
import com.zhuinden.rxrealm.path.cat.Cat;
import com.zhuinden.rxrealm.path.dog.Dog;

import java.util.concurrent.Callable;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.Subscriptions;

import static com.zhuinden.rxrealm.path.dog.DogNames.Dog;

/**
 * Created by Zhuinden on 2016.07.07..
 */
public class MainScopeListener
        extends Fragment {
//    Realm realm;
//
//    public MainScopeListener() {
//        setRetainInstance(true);
//        realm = Realm.getInstance(CustomApplication.get().realmConfiguration);
//        Injector.INSTANCE.initializeComponent(realm);
//    }
//
//    public void configureRealmHolder(MainActivity.RealmHolder realmHolder) {
//        realmHolder.realm = this.realm;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        realm.close();
//    }

    Realm realm;

    final HandlerThread handlerThread;

    Scheduler looperScheduler;

    Observable<Realm> realmObservable;
    Observable<RealmResults<Dog>> dogTableListener;
    Observable<RealmResults<Cat>> catTableListener;

    Subscription realmSubscription;
    Subscription dogSubscription;
    Subscription catSubscription;



    public MainScopeListener() {
        setRetainInstance(true);
        realm = Realm.getDefaultInstance();
        Injector.INSTANCE.initializeComponent(realm);

        // a background Realm looper, this is an experiment.
        handlerThread = new HandlerThread("REALM_LOOPER");
        handlerThread.start();

        synchronized(handlerThread) {
            looperScheduler = AndroidSchedulers.from(handlerThread.getLooper());
        }
        realmObservable = realm.asObservable().unsubscribeOn(looperScheduler).subscribeOn(looperScheduler);
        realmSubscription = realmObservable.subscribe(realm12 -> {
            Log.i("REALM SUBSCRIPTION", "An event occurred on background thread!");
        });
        dogTableListener = Observable.create(new Observable.OnSubscribe<RealmResults<Dog>>() {
            @Override
            public void call(Subscriber<? super RealmResults<Dog>> subscriber) {
                final Realm observableRealm = Realm.getInstance(realm.getConfiguration());
                final RealmResults<Dog> dogTable = observableRealm.where(Dog.class).findAll();

                final RealmChangeListener<RealmResults<Dog>> listener = dogs -> {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(dogs);
                    }
                };
                subscriber.add(Subscriptions.create(() -> {
                    if(dogTable.isValid()) {
                        dogTable.removeChangeListener(listener);
                    }
                    observableRealm.close();
                }));
                dogTable.addChangeListener(listener);

                subscriber.onNext(dogTable);
            }
        }).subscribeOn(looperScheduler).unsubscribeOn(looperScheduler);

        catTableListener = Observable.create(new Observable.OnSubscribe<RealmResults<Cat>>() {
            @Override
            public void call(Subscriber<? super RealmResults<Cat>> subscriber) {
                final Realm observableRealm = Realm.getInstance(realm.getConfiguration());
                final RealmResults<Cat> catTable = observableRealm.where(Cat.class).findAll();

                final RealmChangeListener<RealmResults<Cat>> listener = cats -> {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(cats);
                    }
                };
                subscriber.add(Subscriptions.create(() -> {
                    if(catTable.isValid()) {
                        catTable.removeChangeListener(listener);
                    }
                    observableRealm.close();
                }));
                catTable.addChangeListener(listener);

                subscriber.onNext(catTable);
            }
        }).subscribeOn(looperScheduler).unsubscribeOn(looperScheduler);
        
        dogSubscription = dogTableListener.subscribe(dogs -> {
            Log.i("DOG SUBSCRIPTION", "Event happened for DOG table!");
        });

        catSubscription = catTableListener.subscribe(cats -> {
            Log.i("CAT SUBSCRIPTION", "Event happened for CAT table!");
        });
    }

    public void configureRealmHolder(MainActivity.RealmHolder realmHolder) {
        realmHolder.realm = this.realm;
    }

    @Override
    public void onDestroy() {
        if(realmSubscription != null && !realmSubscription.isUnsubscribed() ) {
            realmSubscription.unsubscribe();
        }
        if(dogSubscription != null && !dogSubscription.isUnsubscribed()) {
            dogSubscription.unsubscribe();
        }
        if(catSubscription!= null && !catSubscription.isUnsubscribed()) {
            catSubscription.unsubscribe();
        }
        handlerThread.quit();
        realm.close();
        super.onDestroy();
    }
}
