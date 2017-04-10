package com.zhuinden.rxrealm.util;

import android.support.v7.widget.RecyclerView;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.MainThreadDisposable;

/**
 * Created by Zhuinden on 2016.07.29..
 */
public final class RecyclerViewScrollBottomOnSubscribe
        implements ObservableOnSubscribe<Boolean> {
    final RecyclerView view;

    public RecyclerViewScrollBottomOnSubscribe(RecyclerView view) {
        this.view = view;
    }

    @Override
    public void subscribe(ObservableEmitter<Boolean> emitter)
            throws Exception {
        MainThreadDisposable.verifyMainThread();

        final RecyclerView.OnScrollListener watcher = new RecyclerView.OnScrollListener() {
            @Override
            public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(!recyclerView.canScrollVertically(1)) {
                    onScrolledToBottom();
                }
            }

            public void onScrolledToBottom() {
                if(!emitter.isDisposed()) {
                    emitter.onNext(true);
                }
            }
        };

        emitter.setDisposable(new MainThreadDisposable() {
            @Override
            protected void onDispose() {
                view.removeOnScrollListener(watcher);
            }
        });

        view.addOnScrollListener(watcher);

        // Emit initial value.
        emitter.onNext(false);
    }
}

