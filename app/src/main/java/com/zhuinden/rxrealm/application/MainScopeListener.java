package com.zhuinden.rxrealm.application;

import android.support.v4.app.Fragment;

import com.zhuinden.rxrealm.application.CustomApplication;
import com.zhuinden.rxrealm.application.MainActivity;

import io.realm.Realm;

/**
 * Created by Zhuinden on 2016.07.07..
 */
public class MainScopeListener
        extends Fragment {
    Realm realm;

    public MainScopeListener() {
        setRetainInstance(true);
        realm = Realm.getInstance(CustomApplication.get().realmConfiguration);
    }

    public void configureRealmHolder(MainActivity.RealmHolder realmHolder) {
        realmHolder.realm = this.realm;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
