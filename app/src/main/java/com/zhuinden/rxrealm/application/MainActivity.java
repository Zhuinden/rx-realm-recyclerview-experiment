package com.zhuinden.rxrealm.application;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.zhuinden.rxrealm.R;
import com.zhuinden.rxrealm.path.dog.DogKey;
import com.zhuinden.rxrealm.util.LayoutKey;
import com.zhuinden.rxrealm.util.OldDispatcherUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import flowless.Direction;
import flowless.Flow;
import flowless.Traversal;
import flowless.TraversalCallback;
import flowless.ViewUtils;
import flowless.preset.DispatcherUtils;
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
        flowDispatcher = new SingleRootDispatcher() {
            @Override
            public void dispatch(@NonNull Traversal traversal, @NonNull TraversalCallback callback) {
                final ViewGroup root = rootHolder.getRoot();
                if(flowless.preset.DispatcherUtils.isPreviousKeySameAsNewKey(traversal.origin, traversal.destination)) { //short circuit on same key
                    callback.onTraversalCompleted();
                    return;
                }
                final LayoutKey newKey = flowless.preset.DispatcherUtils.getNewKey(traversal);
                final LayoutKey previousKey = flowless.preset.DispatcherUtils.getPreviousKey(traversal);

                final Direction direction = traversal.direction;

                final View previousView = root.getChildAt(0);
                flowless.preset.DispatcherUtils.persistViewToStateAndNotifyRemoval(traversal, previousView);

                final View newView = OldDispatcherUtils.createViewFromKey(traversal, newKey, root, baseContext);
                flowless.preset.DispatcherUtils.restoreViewFromState(traversal, newView);

                final LayoutKey animatedKey = OldDispatcherUtils.selectAnimatedKey(direction, previousKey, newKey);
                OldDispatcherUtils.addViewToGroupForKey(direction, newView, root, animatedKey);

                ViewUtils.waitForMeasure(newView, new ViewUtils.OnMeasuredCallback() {
                    @Override
                    public void onMeasured(View view, int width, int height) {
                        Animator animator = OldDispatcherUtils.createAnimatorForViews(animatedKey, previousView, newView, direction);
                        if(animator != null) {
                            animator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    finishTransition(previousView, root, callback);
                                }
                            });
                            animator.start();
                        } else {
                            finishTransition(previousView, root, callback);
                        }
                    }
                });
            }

            private void finishTransition(View previousView, ViewGroup root, @NonNull TraversalCallback callback) {
                OldDispatcherUtils.removeViewFromGroup(previousView, root);
                callback.onTraversalCompleted();
            }
        };
        newBase = Flow.configure(newBase, this) //
                .defaultKey(DogKey.create()) //
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
        flowDispatcher.preSaveViewState();
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
