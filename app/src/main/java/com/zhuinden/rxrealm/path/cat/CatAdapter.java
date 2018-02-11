package com.zhuinden.rxrealm.path.cat;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.zhuinden.rxrealm.R;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Zhuinden on 2016.07.29..
 */
public class CatAdapter
        extends RealmRecyclerViewAdapter<Cat, CatViewHolder> {
    public CatAdapter(@Nullable OrderedRealmCollection<Cat> data) {
        super(data, true);
    }

    @Override
    public CatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_cat_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CatViewHolder holder, int position) {
        Cat cat = getItem(position);
        holder.bind(cat);
    }
}
