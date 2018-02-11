package com.zhuinden.rxrealm.path.cat;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.zhuinden.rxrealm.R;
import com.zhuinden.simplestack.navigator.StateKey;
import com.zhuinden.simplestack.navigator.ViewChangeHandler;
import com.zhuinden.simplestack.navigator.changehandlers.SegueViewChangeHandler;

/**
 * Created by Zhuinden on 2016.07.28..
 */
@AutoValue
public abstract class CatKey
        implements StateKey, Parcelable {
    public static CatKey create() {
        return new AutoValue_CatKey();
    }

    @Override
    public int layout() {
        return R.layout.path_second;
    }

    @NonNull
    @Override
    public ViewChangeHandler viewChangeHandler() {
        return new SegueViewChangeHandler();
    }
}
