package com.zhuinden.rxrealm;

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
import com.zhuinden.rxrealm.application.MainActivity;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import flowless.ActivityUtils;
import flowless.Bundleable;
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
public class FirstView
        extends RelativeLayout
        implements FlowLifecycles.ViewLifecycleListener, Bundleable {
    private static final String TAG = "FirstView";

    public FirstView(Context context) {
        super(context);
    }

    public FirstView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FirstView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public FirstView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @BindView(R.id.first_dog_edittext)
    EditText editText;

    @BindView(R.id.first_recyclerview)
    RecyclerView recyclerView;

    CompositeSubscription subscription;
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
    public void onViewRestored(boolean forcedWithBundler) {
        MainActivity mainActivity = (MainActivity) ActivityUtils.getActivity(getContext());
        realm = mainActivity.getRealm();

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
            query = query.contains(Dog.Fields.NAME.getField(), selectedName, Case.INSENSITIVE);
        }
        RealmResults<Dog> results = query.findAllSortedAsync(Dog.Fields.NAME.getField());
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
                    long currentIndex = bgRealm.where(Dog.class).max(Dog.Fields.ID.getField()).longValue();
                    Dog dog = new Dog();
                    dog.setId(currentIndex + 1);
                    dog.setName(DogNames.values()[((Long) dog.getId()).intValue() % DogNames.values().length].name());
                    dog = bgRealm.copyToRealmOrUpdate(dog);
                    Log.i(TAG, "Realm write successful [" + counter + "] :: [" + dog.getName() + "].");
                    counter++;
                })).subscribe();
    }
}
