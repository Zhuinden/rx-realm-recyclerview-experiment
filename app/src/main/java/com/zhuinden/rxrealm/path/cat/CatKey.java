package com.zhuinden.rxrealm.path.cat;

import com.google.auto.value.AutoValue;
import com.zhuinden.rxrealm.R;
import com.zhuinden.rxrealm.util.FlowAnimation;
import com.zhuinden.rxrealm.util.LayoutKey;

/**
 * Created by Zhuinden on 2016.07.28..
 */
@AutoValue
public abstract class CatKey
        implements LayoutKey {
    public static CatKey create() {
        return new AutoValue_CatKey(R.layout.path_second, FlowAnimation.SEGUE);
    }
}
