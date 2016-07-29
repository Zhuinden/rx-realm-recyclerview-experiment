package com.zhuinden.rxrealm.path.cat;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhuinden.rxrealm.R;
import com.zhuinden.rxrealm.application.CustomApplication;
import com.zhuinden.rxrealm.application.MainActivity;
import com.zhuinden.rxrealm.util.RecyclerViewScrollBottomOnSubscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import flowless.ActivityUtils;
import flowless.preset.FlowLifecycles;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
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
    }

    public CatView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public CatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @BindView(R.id.second_recyclerview)
    RecyclerView recyclerView;

    Realm realm;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }


    CompositeSubscription compositeSubscription;

    @Override
    public void onViewRestored(boolean forcedWithBundler) {
        compositeSubscription = new CompositeSubscription();

        MainActivity mainActivity = (MainActivity) ActivityUtils.getActivity(getContext());
        realm = mainActivity.getRealm();

        recyclerView.setAdapter(new RealmRecyclerViewAdapter<Cat, CatViewHolder>(getContext(),
                realm.where(Cat.class).findAllSortedAsync(Cat.Fields.RANK.getField(), Sort.ASCENDING),
                true) {
            @Override
            public CatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new CatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_cat_item, parent, false));
            }

            @Override
            public void onBindViewHolder(CatViewHolder holder, int position) {
                Cat cat = getItem(position);
                if(cat != null) {
                    Glide.with(getContext()).load(cat.getUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.image);
                    holder.sourceUrl.setText(cat.getSourceUrl());
                }
            }
        });

        CatService catService = CustomApplication.get().catService;
        Subscription downloadCats = Observable.create(new RecyclerViewScrollBottomOnSubscribe(recyclerView))
                .switchMap(aVoid -> catService.getCats().subscribeOn(Schedulers.io()))
                .subscribe(catsBO -> {
                    if(Looper.myLooper() != null) {
                        throw new IllegalStateException("Expected to be called in io() scheduler but was called on main thread");
                    }
                    Realm realm = null;
                    try {
                        realm = Realm.getInstance(CustomApplication.get().realmConfiguration);
                        Cat defaultCat = new Cat();
                        realm.executeTransaction(realm1 -> {
                            long rank;
                            if(realm1.where(Cat.class).count() > 0) {
                                rank = realm1.where(Cat.class).max(Cat.Fields.RANK.getField()).longValue();
                            } else {
                                rank = 0;
                            }
                            for(CatBO catBO : catsBO.getCats()) {
                                defaultCat.setId(catBO.getId());
                                defaultCat.setRank(++rank);
                                defaultCat.setSourceUrl(catBO.getSourceUrl());
                                defaultCat.setUrl(catBO.getUrl());
                                realm1.insertOrUpdate(defaultCat);
                            }
                        });
                    } finally {
                        if(realm != null) {
                            realm.close();
                        }
                    }
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

    public static class CatViewHolder
            extends RecyclerView.ViewHolder {
        @BindView(R.id.cat_image)
        ImageView image;

        @BindView(R.id.cat_source_url)
        TextView sourceUrl;

        public CatViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
