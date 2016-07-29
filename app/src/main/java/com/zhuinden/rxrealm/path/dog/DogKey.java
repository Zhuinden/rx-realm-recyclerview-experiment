package com.zhuinden.rxrealm.path.dog;

import com.google.auto.value.AutoValue;
import com.zhuinden.rxrealm.R;

import flowless.preset.FlowAnimation;
import flowless.preset.LayoutKey;

/**
 * Created by Zhuinden on 2016.07.07..
 */
@AutoValue
public abstract class DogKey
        implements LayoutKey {
    public static DogKey create() {
        return new AutoValue_DogKey(R.layout.path_first, FlowAnimation.SEGUE);
    }
}
