package com.zhuinden.rxrealm.path.dog;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.zhuinden.rxrealm.R;
import com.zhuinden.simplestack.navigator.StateKey;
import com.zhuinden.simplestack.navigator.ViewChangeHandler;
import com.zhuinden.simplestack.navigator.changehandlers.SegueViewChangeHandler;


/**
 * Created by Zhuinden on 2016.07.07..
 */
@AutoValue
public abstract class DogKey
        implements StateKey, Parcelable {
    public static DogKey create() {
        return new AutoValue_DogKey();
    }

    @Override
    public int layout() {
        return R.layout.path_first;
    }

    @NonNull
    @Override
    public ViewChangeHandler viewChangeHandler() {
        return new SegueViewChangeHandler();
    }
}
