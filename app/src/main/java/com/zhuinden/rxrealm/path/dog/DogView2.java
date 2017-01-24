package com.zhuinden.rxrealm.path.dog;

import android.annotation.TargetApi;
import android.content.Context;
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
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by Owner on 2017. 01. 24..
 */

public class DogView2
        extends RelativeLayout
        implements FlowLifecycles.ViewLifecycleListener, Bundleable {
    private static final String TAG = "DogView";

    public DogView2(Context context) {
        super(context);
        init();
    }

    public DogView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DogView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public DogView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    CompositeSubscription subscription;

    @Inject
    Realm realm;

    volatile int counter = 0;
    DogAdapter adapter;
    String currentName;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    public static class DogAdapter extends RecyclerView.Adapter<DogViewHolder> {
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

    @Override
    public void onViewRestored() {
        adapter = new DogAdapter();
        recyclerView.setAdapter(adapter);

        subscription = new CompositeSubscription();
        subscription.add(readFromEditText());
        subscription.add(writePeriodic());
    }

    @Override
    public void onViewDestroyed(boolean removedByFlow) {
        subscription.unsubscribe();
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

    private Observable<List<Dog>> getDogs(final String selectedName) {
        return Observable.create(new Observable.OnSubscribe<List<Dog>>() {
            @Override
            public void call(Subscriber<? super List<Dog>> subscriber) {
                Log.i("DOG OBSERVABLE", "Creating observable for [" + selectedName + "]");
                RealmConfiguration realmConfiguration = realm.getConfiguration();
                Realm observableRealm = Realm.getInstance(realmConfiguration);

                final RealmChangeListener<RealmResults<Dog>> listener = dogs -> {
                    if(!subscriber.isUnsubscribed()) {
                        subscriber.onNext(observableRealm.copyFromRealm(dogs));
                    }
                };

                RealmQuery<Dog> query = observableRealm.where(Dog.class);
                if(selectedName != null && !"".equals(selectedName)) {
                    query = query.contains(DogFields.NAME, selectedName, Case.INSENSITIVE);
                }
                final RealmResults<Dog> dogTable = query.findAllSorted(DogFields.NAME);
                subscriber.add(Subscriptions.create(() -> {
                    Log.i("DOG OBSERVABLE", "Unsubscribing.");
                    if(dogTable.isValid()) {
                        dogTable.removeChangeListener(listener);
                    }
                    observableRealm.close();
                }));
                dogTable.addChangeListener(listener);
                subscriber.onNext(observableRealm.copyFromRealm(dogTable));
            }
        }).subscribeOn(MainScopeListener.LOOPER_SCHEDULER).unsubscribeOn(MainScopeListener.LOOPER_SCHEDULER);
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

    private Subscription readFromEditText() {
        return RxTextView.textChanges(editText).switchMap(charSequence -> {
            currentName = charSequence.toString();
            return getDogs(currentName);
        }).observeOn(Schedulers.io()) //
                .map(newDogs -> Pair.with(DiffUtil.calculateDiff(new DogDiffCallback(adapter.dogs, newDogs)), newDogs)) //
                .observeOn(AndroidSchedulers.mainThread()) //
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

    private Subscription writePeriodic() {
        return Observable.interval(2000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) //
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
