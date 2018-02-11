package com.zhuinden.rxrealm.application;

import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.zhuinden.rxrealm.application.injection.Injector;
import com.zhuinden.rxrealm.path.cat.Cat;
import com.zhuinden.rxrealm.path.dog.Dog;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Consumer;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Zhuinden on 2016.07.07..
 */
public class MainScopeListener
        extends Fragment {
    Realm realm;

    final HandlerThread handlerThread;
    final HandlerThread handlerThread2;

    public static Scheduler LOOPER_SCHEDULER;
    public static io.reactivex.Scheduler LOOPER_SCHEDULER_2;

    Observable<Realm> realmObservable;
    Observable<RealmResults<Dog>> dogTableListener;
    Observable<RealmResults<Cat>> catTableListener;

    Disposable realmDisposable;
    Disposable dogDisposable;
    Disposable catDisposable;
    
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
        realmObservable = realm.asFlowable().toObservable().unsubscribeOn(LOOPER_SCHEDULER).subscribeOn(LOOPER_SCHEDULER);
        realmDisposable = realmObservable.subscribe(new Consumer<Realm>() {
            @Override
            public void accept(Realm realm)
                    throws Exception {
                Log.i("REALM SUBSCRIPTION", "An event occurred on background thread!");
            }
        });
        dogTableListener = Observable.create(new ObservableOnSubscribe<RealmResults<Dog>>() {
            @Override
            public void subscribe(ObservableEmitter<RealmResults<Dog>> subscriber) {
                final Realm observableRealm = Realm.getInstance(realm.getConfiguration());
                final RealmResults<Dog> dogTable = observableRealm.where(Dog.class).findAll();

                final RealmChangeListener<RealmResults<Dog>> listener = dogs -> {
                    if(!subscriber.isDisposed()) {
                        subscriber.onNext(dogs);
                    }
                };
                subscriber.setDisposable(Disposables.fromAction(() -> {
                    if(dogTable.isValid()) {
                        dogTable.removeChangeListener(listener);
                    }
                    observableRealm.close();
                }));
                dogTable.addChangeListener(listener);

                subscriber.onNext(dogTable);
            }
        }).subscribeOn(LOOPER_SCHEDULER).unsubscribeOn(LOOPER_SCHEDULER);

        catTableListener = Observable.create(new ObservableOnSubscribe<RealmResults<Cat>>() {
            @Override
            public void subscribe(ObservableEmitter<RealmResults<Cat>> emitter) {
                final Realm observableRealm = Realm.getInstance(realm.getConfiguration());
                final RealmResults<Cat> catTable = observableRealm.where(Cat.class).findAll();

                final RealmChangeListener<RealmResults<Cat>> listener = cats -> {
                    if(!emitter.isDisposed()) {
                        emitter.onNext(cats);
                    }
                };
                emitter.setDisposable(Disposables.fromAction(() -> {
                    if(catTable.isValid()) {
                        catTable.removeChangeListener(listener);
                    }
                    observableRealm.close();
                }));
                catTable.addChangeListener(listener);

                emitter.onNext(catTable);
            }
        }).subscribeOn(LOOPER_SCHEDULER).unsubscribeOn(LOOPER_SCHEDULER);

        dogDisposable = dogTableListener.subscribe(dogs -> {
            Log.i("DOG SUBSCRIPTION", "Event happened for DOG table! [" + Thread.currentThread() + "]");
        });

        catDisposable = catTableListener.subscribe(cats -> {
            Log.i("CAT SUBSCRIPTION", "Event happened for CAT table! [" + Thread.currentThread() + "]");
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
        if(realmDisposable != null && !realmDisposable.isDisposed()) {
            realmDisposable.dispose();
        }
        if(dogDisposable != null && !dogDisposable.isDisposed()) {
            dogDisposable.dispose();
        }
        if(catDisposable != null && !catDisposable.isDisposed()) {
            catDisposable.dispose();
        }
        handlerThread.quit();
        realm.close();
        super.onDestroy();
    }
}
