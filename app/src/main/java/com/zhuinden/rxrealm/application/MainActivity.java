package com.zhuinden.rxrealm.application;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.zhuinden.rxrealm.FirstKey;
import com.zhuinden.rxrealm.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import flowless.Flow;
import flowless.preset.SingleRootDispatcher;
import io.realm.Realm;

public class MainActivity
        extends AppCompatActivity {
    public static class RealmHolder {
        Realm realm;

        public Realm getRealm() {
            return realm;
        }
    }

    SingleRootDispatcher flowDispatcher;

    @Override
    protected void attachBaseContext(Context newBase) {
        flowDispatcher = new SingleRootDispatcher(this);
        newBase = Flow.configure(newBase, this) //
                .defaultKey(FirstKey.create()) //
                .dispatcher(flowDispatcher) //
                .install(); //
        flowDispatcher.setBaseContext(newBase);
        super.attachBaseContext(newBase);
    }

    @BindView(R.id.main_root)
    ViewGroup root;

    RealmHolder realmHolder;

    public Realm getRealm() {
        return realmHolder.getRealm();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        flowDispatcher.getRootHolder().setRoot(root);
        realmHolder = new RealmHolder();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("MAIN_SCOPE_LISTENER");
        if(fragment == null) {
            fragment = new MainScopeListener();
            getSupportFragmentManager().beginTransaction().add(fragment, "MAIN_SCOPE_LISTENER").commit();
        }
        ((MainScopeListener)fragment).configureRealmHolder(realmHolder);
    }

    @Override
    public void onBackPressed() {
        if(!flowDispatcher.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        flowDispatcher.preSaveViewState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        flowDispatcher.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        flowDispatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
