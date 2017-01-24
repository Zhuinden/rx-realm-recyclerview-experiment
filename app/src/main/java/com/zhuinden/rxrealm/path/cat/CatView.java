package com.zhuinden.rxrealm.path.cat;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.zhuinden.rxrealm.R;
import com.zhuinden.rxrealm.application.injection.Injector;
import com.zhuinden.rxrealm.util.RecyclerViewScrollBottomOnSubscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import flowless.preset.FlowLifecycles;
import io.realm.Realm;
import io.realm.Sort;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Zhuinden on 2016.07.28..
 */
public class CatView
        extends RelativeLayout
        implements FlowLifecycles.ViewLifecycleListener {
    private static final String TAG = "CatView";

    public CatView(Context context) {
        super(context);
        init();
    }

    public CatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public CatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if(!isInEditMode()) {
            Injector.INSTANCE.getComponent().inject(this);
        }
    }

    @BindView(R.id.second_recyclerview)
    RecyclerView recyclerView;

    @Inject
    Realm realm;

    @Inject
    CatPersister catPersister;

    @Inject
    CatService catService;

    CompositeSubscription compositeSubscription;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onViewRestored() {
        compositeSubscription = new CompositeSubscription();

        recyclerView.setAdapter(new CatAdapter(getContext(), realm.where(Cat.class).findAllSortedAsync(CatFields.RANK, Sort.ASCENDING)));
        Subscription downloadCats = Observable.create(new RecyclerViewScrollBottomOnSubscribe(recyclerView))
                .filter(isScroll -> isScroll || realm.where(Cat.class).count() <= 0)
                .switchMap(isScroll -> catService.getCats().subscribeOn(Schedulers.io())) //
                .retry()
                .subscribe(catsBO -> {
                    catPersister.persist(catsBO);
                }, throwable -> {
                    Log.e(TAG, "An error occurred", throwable);
                });
        compositeSubscription.add(downloadCats);
    }

    @Override
    public void onViewDestroyed(boolean removedByFlow) {
        if(!compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }
}
