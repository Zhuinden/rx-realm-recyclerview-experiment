package com.zhuinden.rxrealm.path.dog;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.zhuinden.rxrealm.R;
import com.zhuinden.rxrealm.application.injection.Injector;
import com.zhuinden.rxrealm.path.cat.CatKey;
import com.zhuinden.simplestack.Bundleable;
import com.zhuinden.simplestack.navigator.Navigator;
import com.zhuinden.statebundle.StateBundle;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by Zhuinden on 2016.07.07..
 */
public class DogView
        extends RelativeLayout
        implements Bundleable {
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
            Injector.get().inject(this);
        }
    }

    @BindView(R.id.first_dog_edittext)
    EditText editText;

    @BindView(R.id.first_recyclerview)
    RecyclerView recyclerView;

    @OnClick(R.id.first_go_to_cat)
    public void goToCat() {
        Navigator.getBackstack(getContext()).goTo(CatKey.create());
    }

    CompositeDisposable subscription;

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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        adapter = new RealmRecyclerViewAdapter<Dog, DogViewHolder>(getDogs(currentName), true) {
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

        subscription = new CompositeDisposable();
        subscription.add(readFromEditText());
        subscription.add(writePeriodic());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subscription.dispose();
    }

    @NonNull
    @Override
    public StateBundle toBundle() {
        StateBundle bundle = new StateBundle();
        bundle.putInt("counter", counter);
        bundle.putString("currentName", currentName);
        return bundle;
    }

    @Override
    public void fromBundle(@Nullable StateBundle bundle) {
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

    private Disposable readFromEditText() {
        return RxTextView.textChanges(editText).doOnNext(charSequence -> currentName = charSequence.toString())
                .toFlowable(BackpressureStrategy.LATEST)//
                .switchMap(charSequence -> getDogs(currentName).asFlowable()) //
                .filter(RealmResults::isLoaded) //
                .subscribe(dogs -> adapter.updateData(dogs));
    }

    private Disposable writePeriodic() {
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
