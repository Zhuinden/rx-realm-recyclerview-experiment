package com.zhuinden.rxrealm.path.cat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhuinden.rxrealm.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import flowless.ActivityUtils;

/**
 * Created by Zhuinden on 2016.07.29..
 */
public class CatViewHolder
        extends RecyclerView.ViewHolder {
    private String _sourceUrl;

    @OnClick(R.id.cat_item_container)
    public void openCat(View view) {
        if(_sourceUrl != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(_sourceUrl));
            ActivityUtils.getActivity(view.getContext()).startActivity(intent);
        }
    }

    @BindView(R.id.cat_image)
    ImageView image;

    @BindView(R.id.cat_source_url)
    TextView sourceUrl;

    public CatViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Context context, Cat cat) {
        if(cat != null) {
            Glide.with(context).load(cat.getUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).into(image);
            sourceUrl.setText(cat.getSourceUrl());
            _sourceUrl = cat.getSourceUrl();
        } else {
            Glide.clear(image);
            sourceUrl.setText("");
            _sourceUrl = null;
        }

    }
}