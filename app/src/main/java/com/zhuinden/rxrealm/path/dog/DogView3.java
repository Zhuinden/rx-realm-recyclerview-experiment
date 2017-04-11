package com.zhuinden.rxrealm.path.dog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.zhuinden.rxrealm.R;
import com.zhuinden.rxrealm.application.MainScopeListener;
import com.zhuinden.rxrealm.application.injection.Injector;
import com.zhuinden.rxrealm.path.cat.CatKey;

import org.javatuples.Pair;
import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import flowless.Bundleable;
import flowless.Flow;
import flowless.preset.FlowLifecycles;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.BackpressureStrategy;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Function;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Owner on 2017. 01. 24..
 */

public class DogView3
        extends RelativeLayout
        implements FlowLifecycles.ViewLifecycleListener, Bundleable {
    private static final String TAG = "DogView";

    public DogView3(Context context) {
        super(context);
        init();
    }

    public DogView3(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DogView3(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public DogView3(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if(!isInEditMode()) {
            Injector.INSTANCE.getComponent().inject(this);
        }
    }

    @BindView(R.id.first_dog_edittext)
    EditText editText;

    @BindView(R.id.first_recyclerview)
    RecyclerView recyclerView;

    @OnClick(R.id.first_go_to_cat)
    public void goToCat() {
        Flow.get(this).set(CatKey.create());
    }

    CompositeDisposable subscription;

    @Inject
    Realm realm;

    volatile int counter = 0;
    DogAdapter adapter;
    String currentName;

    @Override
    @SuppressWarnings("NewApi")
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    public static class DogAdapter
            extends RecyclerView.Adapter<DogViewHolder> {
        private List<Dog> dogs = Collections.emptyList();

        @Override
        public DogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_dog_item, parent, false));
        }

        @Override
        public void onBindViewHolder(DogViewHolder holder, int position) {
            Dog dog = dogs.get(position);
            holder.name.setText(dog.getName());
        }

        @Override
        public int getItemCount() {
            return dogs == null ? 0 : dogs.size();
        }

        public void setNewData(List<Dog> dogs) {
            this.dogs = dogs;
        }
    }

    RealmChangeListener<Realm> realmRealmChangeListener = element -> Log.i("DOG_",
            "REALM CHANGE HAPPENED [" + Thread.currentThread() + "]");

    @Override
    @SuppressWarnings("NewApi")
    public void onViewRestored() {
        adapter = new DogAdapter();
        recyclerView.setAdapter(adapter);

        subscription = new CompositeDisposable();
        subscription.add(readFromEditText());
        //subscription.add(writePeriodic());

        realm.addChangeListener(realmRealmChangeListener);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try(Realm r = Realm.getDefaultInstance()) {
                    r.executeTransaction(realm -> {
                        Dog dog = new Dog();
                        dog.setId(5151L);
                        dog.setName("HELLO!");
                        realm.insertOrUpdate(dog);
                        Log.i("DOG", "INSERTING DOG [" + dog + "] [" + Thread.currentThread() + "]");
                    });
                    Log.i("DOG", "INSERTED DOG! [" + Thread.currentThread() + "]");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Dog dog = realm.where(Dog.class).equalTo(DogFields.ID, 5151L).findFirst();
                if(dog != null) {
                    Log.i("DOG", "DOG IS FOUND [" + dog + "] [" + Thread.currentThread() + "]");
                } else {
                    Log.i("DOG", "SAD :( [" + Thread.currentThread() + "]");
                }
            }
        }.execute();
    }

    @Override
    public void onViewDestroyed(boolean removedByFlow) {
        realm.removeChangeListener(realmRealmChangeListener);
        subscription.dispose();
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("counter", counter);
        bundle.putString("currentName", currentName);
        return bundle;
    }

    @Override
    public void fromBundle(@Nullable Bundle bundle) {
        if(bundle != null) {
            counter = bundle.getInt("counter");
            currentName = bundle.getString("currentName", null);
        }
    }

    private io.reactivex.Flowable<List<Dog>> getDogs(final String selectedName) {
        return io.reactivex.Flowable.create(new FlowableOnSubscribe<List<Dog>>() {
            @Override
            public void subscribe(FlowableEmitter<List<Dog>> emitter)
                    throws Exception {
                Log.i("DOG FLOWABLE", "Creating flowable for [" + selectedName + "]");
                RealmConfiguration realmConfiguration = realm.getConfiguration();
                Realm observableRealm = Realm.getInstance(realmConfiguration);

                final RealmChangeListener<RealmResults<Dog>> listener = dogs -> {
                    emitter.onNext(observableRealm.copyFromRealm(dogs));
                };

                RealmQuery<Dog> query = observableRealm.where(Dog.class);
                if(selectedName != null && !"".equals(selectedName)) {
                    query = query.contains(DogFields.NAME, selectedName, Case.INSENSITIVE);
                }
                final RealmResults<Dog> dogTable = query.findAllSorted(DogFields.NAME);
                emitter.setDisposable(Disposables.fromRunnable(() -> {
                    Log.i("DOG FLOWABLE", "Unsubscribing.");
                    if(dogTable.isValid()) {
                        dogTable.removeChangeListener(listener);
                    }
                    observableRealm.close();
                }));
                dogTable.addChangeListener(listener);
                emitter.onNext(observableRealm.copyFromRealm(dogTable));
            }
        }, BackpressureStrategy.LATEST).subscribeOn(MainScopeListener.LOOPER_SCHEDULER_2).unsubscribeOn(MainScopeListener.LOOPER_SCHEDULER_2);
    }

    public static class DogDiffCallback
            extends DiffUtil.Callback {
        private List<Dog> oldDogs;
        private List<Dog> newDogs;

        public DogDiffCallback(List<Dog> oldDogs, List<Dog> newDogs) {
            this.oldDogs = oldDogs;
            this.newDogs = newDogs;
        }

        @Override
        public int getOldListSize() {
            return oldDogs == null ? 0 : oldDogs.size();
        }

        @Override
        public int getNewListSize() {
            return newDogs.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return newDogs.get(newItemPosition).getId() == oldDogs.get(oldItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return newDogs.get(newItemPosition).getName().equals(oldDogs.get(oldItemPosition).getName());
        }
    }

    private Disposable readFromEditText() {
        return RxJavaInterop.toV2Flowable(RxTextView.textChanges(editText))
                .switchMap(new Function<CharSequence, Publisher<List<Dog>>>() {
                    @Override
                    public Publisher<List<Dog>> apply(CharSequence charSequence)
                            throws Exception {
                        currentName = charSequence.toString();
                        return getDogs(currentName);
                    }
                })
                .observeOn(io.reactivex.schedulers.Schedulers.computation()) //
                .map(newDogs -> Pair.with(DiffUtil.calculateDiff(new DogDiffCallback(adapter.dogs, newDogs)), newDogs)) //
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread()) //
                .subscribe(pairOfDiffResultAndNewDogs -> {
                    List<Dog> newDogs = pairOfDiffResultAndNewDogs.getValue1();
                    adapter.setNewData(newDogs);
                    DiffUtil.DiffResult diffResult = pairOfDiffResultAndNewDogs.getValue0();
                    diffResult.dispatchUpdatesTo(adapter);
                    //Log.d(TAG, "Update with size [" + dogs.size() + "] for name [" + currentName + "]");
                    //adapter.setNewData(dogs);
                });
    }

    /*private Subscription readFromEditText() {
        return RxTextView.textChanges(editText).switchMap(charSequence -> {
            currentName = charSequence.toString();
            return getDogs(currentName);
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(dogs -> {
            Log.d(TAG, "Update with size [" + dogs.size() + "] for name [" + currentName + "]");
            adapter.setNewData(dogs);
            adapter.notifyDataSetChanged();
        });
    }
    */

    private Disposable writePeriodic() {
        return io.reactivex.Observable.interval(2000,
                TimeUnit.MILLISECONDS,
                io.reactivex.android.schedulers.AndroidSchedulers.mainThread()) //
                .takeWhile(aLong -> counter < DogNames.values().length) //
                .doOnNext(aLong -> realm.executeTransactionAsync(bgRealm -> { //
                    long currentIndex = bgRealm.where(Dog.class).max(DogFields.ID).longValue();
                    Dog dog = new Dog();
                    dog.setId(currentIndex + 1);
                    dog.setName(DogNames.values()[((Long) dog.getId()).intValue() % DogNames.values().length].name());
                    dog = bgRealm.copyToRealmOrUpdate(dog);
                    Log.i(TAG, "Realm write successful [" + counter + "] :: [" + dog.getName() + "].");
                    counter++;
                })).subscribe();
    }

//    @SuppressWarnings("NewApi")
//    private Subscription writePeriodic() {
//        return Observable.interval(2000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) //
//                .takeWhile(aLong -> counter < DogNames.values().length) //
//                .observeOn(Schedulers.io())
//                .doOnNext(aLong -> {
//                    try(Realm bgRealm = Realm.getDefaultInstance()) {
//                        bgRealm.executeTransaction(realm1 -> {
//                            long currentIndex = realm1.where(Dog.class).max(DogFields.ID).longValue();
//                            Dog dog = new Dog();
//                            dog.setId(currentIndex + 1);
//                            dog.setName(DogNames.values()[((Long) dog.getId()).intValue() % DogNames.values().length].name());
//                            dog = realm1.copyToRealmOrUpdate(dog);
//                            Log.i(TAG, "Realm write successful [" + counter + "] :: [" + dog.getName() + "].");
//                            counter++;
//                        });
//                    }
//                }).subscribe();
//    }
}
