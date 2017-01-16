package com.zhuinden.rxrealm.util;

import android.os.Parcelable;
import android.support.annotation.LayoutRes;
/**
 * Created by Owner on 2017. 01. 16..
 */

public interface LayoutKey
        extends Parcelable {
    @LayoutRes
    int layout();

    FlowAnimation animation();
}