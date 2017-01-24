package com.zhuinden.rxrealm.path.dog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.zhuinden.rxrealm.application.injection.Injector;
import com.zhuinden.rxrealm.path.cat.CatKey;

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
import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Zhuinden on 2016.07.07..
 */
public class DogView
        extends RelativeLayout
        implements FlowLifecycles.ViewLifecycleListener, Bundleable {
    private static final String TAG = "DogView";

    public DogView(Context context) {
        super(context);
        init();
    }

    public DogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public DogView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
    RealmRecyclerViewAdapter<Dog, DogViewHolder> adapter;
    String currentName;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onViewRestored() {
        adapter = new RealmRecyclerViewAdapter<Dog, DogViewHolder>(getContext(), getDogs(currentName), true) {
            @Override
            public DogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new DogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_dog_item, parent, false));
            }

            @Override
            public void onBindViewHolder(DogViewHolder holder, int position) {
                Dog dog = getData().get(position);
                holder.name.setText(dog.getName());
            }
        };
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

    private RealmResults<Dog> getDogs(String selectedName) {
        RealmQuery<Dog> query = realm.where(Dog.class);
        if(selectedName != null && !"".equals(selectedName)) {
            query = query.contains(DogFields.NAME, selectedName, Case.INSENSITIVE);
        }
        RealmResults<Dog> results = query.findAllSortedAsync(DogFields.NAME);
        Log.d(TAG, "Result size [" + results.size() + "] for name [" + currentName + "]");
        return results;
    }

    private Subscription readFromEditText() {
        return RxTextView.textChanges(editText).switchMap(charSequence -> {
            currentName = charSequence.toString();
            return getDogs(currentName).asObservable();
        }).filter(RealmResults::isLoaded) //
                .subscribe(dogs -> {
                    Log.d(TAG, "Update with size [" + dogs.size() + "] for name [" + currentName + "]");
                    adapter.updateData(dogs);
                });
    }

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
