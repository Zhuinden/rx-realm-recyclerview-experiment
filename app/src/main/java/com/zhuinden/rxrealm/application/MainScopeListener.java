package com.zhuinden.rxrealm.application;

import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.zhuinden.rxrealm.application.injection.Injector;
import com.zhuinden.rxrealm.path.cat.Cat;
import com.zhuinden.rxrealm.path.dog.Dog;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

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
    final HandlerThread handlerThread2;

    public static Scheduler LOOPER_SCHEDULER;
    public static io.reactivex.Scheduler LOOPER_SCHEDULER_2;

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
            LOOPER_SCHEDULER = AndroidSchedulers.from(handlerThread.getLooper());
        }
        realmObservable = realm.asObservable().unsubscribeOn(LOOPER_SCHEDULER).subscribeOn(LOOPER_SCHEDULER);
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
        }).subscribeOn(LOOPER_SCHEDULER).unsubscribeOn(LOOPER_SCHEDULER);

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
        }).subscribeOn(LOOPER_SCHEDULER).unsubscribeOn(LOOPER_SCHEDULER);
        
        dogSubscription = dogTableListener.subscribe(dogs -> {
            Log.i("DOG SUBSCRIPTION", "Event happened for DOG table!");
        });

        catSubscription = catTableListener.subscribe(cats -> {
            Log.i("CAT SUBSCRIPTION", "Event happened for CAT table!");
        });


        // RXJAVA 2 EXPERIMENT: a background Realm looper, this is an experiment.
        handlerThread2 = new HandlerThread("REALM_LOOPER_2");
        handlerThread2.start();

        synchronized(handlerThread2) {
            LOOPER_SCHEDULER_2 = io.reactivex.android.schedulers.AndroidSchedulers.from(handlerThread2.getLooper());
        }
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
