package com.zhuinden.rxrealm.application;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.zhuinden.rxrealm.application.injection.Injector;

import java.util.concurrent.Callable;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
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

    Scheduler looperScheduler;

    Observable<Realm> realmObservable;

    Subscription realmSubscription;

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
        realmObservable = realm.asObservable();
        realmSubscription = realmObservable.unsubscribeOn(looperScheduler).subscribeOn(looperScheduler).subscribe(realm12 -> {
            Log.i("REALM SUBSCRIPTION", "An event occurred on background thread!");
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
        handlerThread.quit();
        realm.close();
        super.onDestroy();
    }
}
