package com.zhuinden.rxrealm.path.cat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhuinden.rxrealm.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhuinden on 2016.07.29..
 */
public class CatViewHolder
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