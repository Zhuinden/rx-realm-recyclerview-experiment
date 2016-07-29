package com.zhuinden.rxrealm.path.dog;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.zhuinden.rxrealm.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhuinden on 2016.07.07..
 */
public class DogViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.dog_item_name)
    TextView name;

    public DogViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
