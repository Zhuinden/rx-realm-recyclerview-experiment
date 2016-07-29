package com.zhuinden.rxrealm.path.cat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhuinden.rxrealm.R;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Zhuinden on 2016.07.29..
 */
public class CatAdapter
        extends RealmRecyclerViewAdapter<Cat, CatViewHolder> {
    private final Context context;

    public CatAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Cat> data) {
        super(context, data, true);
        this.context = context;
    }

    @Override
    public CatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_cat_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CatViewHolder holder, int position) {
        Cat cat = getItem(position);
        if(cat != null) {
            Glide.with(context).load(cat.getUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.image);
            holder.sourceUrl.setText(cat.getSourceUrl());
        }
    }
}
