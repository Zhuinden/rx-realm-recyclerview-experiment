package com.zhuinden.rxrealm.application;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.zhuinden.rxrealm.R;
import com.zhuinden.rxrealm.path.dog.DogKey;
import com.zhuinden.simplestack.HistoryBuilder;
import com.zhuinden.simplestack.navigator.Navigator;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class MainActivity
        extends AppCompatActivity {
    public static class RealmHolder {
        Realm realm;

        public Realm getRealm() {
            return realm;
        }
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
        realmHolder = new RealmHolder();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("MAIN_SCOPE_LISTENER");
        if(fragment == null) {
            fragment = new MainScopeListener();
            getSupportFragmentManager().beginTransaction().add(fragment, "MAIN_SCOPE_LISTENER").commit();
        }
        ((MainScopeListener)fragment).configureRealmHolder(realmHolder);
        Navigator.install(this, root, HistoryBuilder.single(DogKey.create()));
    }

    @Override
    public void onBackPressed() {
        if(!Navigator.onBackPressed(this)) {
            super.onBackPressed();
        }
    }
}
