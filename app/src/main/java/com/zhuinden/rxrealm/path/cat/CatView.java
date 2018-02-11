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
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by Zhuinden on 2016.07.28..
 */
public class CatView
        extends RelativeLayout {
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
            Injector.get().inject(this);
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

    CompositeDisposable compositeSubscription;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        compositeSubscription = new CompositeDisposable();

        recyclerView.setAdapter(new CatAdapter(realm.where(Cat.class).sort(CatFields.RANK,
                                                                           Sort.ASCENDING).findAllAsync()));
        Disposable downloadCats = Observable.create(new RecyclerViewScrollBottomOnSubscribe(recyclerView))
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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(!compositeSubscription.isDisposed()) {
            compositeSubscription.dispose();
        }
    }
}
