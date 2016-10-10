package com.zhuinden.rxrealm.util;

import android.support.v7.widget.RecyclerView;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 * Created by Zhuinden on 2016.07.29..
 */
public final class RecyclerViewScrollBottomOnSubscribe
        implements Observable.OnSubscribe<Boolean> {
    final RecyclerView view;

    public RecyclerViewScrollBottomOnSubscribe(RecyclerView view) {
        this.view = view;
    }

    @Override
    public void call(final Subscriber<? super Boolean> subscriber) {
        MainThreadSubscription.verifyMainThread();

        final RecyclerView.OnScrollListener watcher = new RecyclerView.OnScrollListener() {
            @Override
            public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(!recyclerView.canScrollVertically(1)) {
                    onScrolledToBottom();
                }
            }

            public void onScrolledToBottom() {
                if(!subscriber.isUnsubscribed()) {
                    subscriber.onNext(true);
                }
            }
        };
            
        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                view.removeOnScrollListener(watcher);
            }
        });
            
        view.addOnScrollListener(watcher);

        // Emit initial value.
        subscriber.onNext(false);
    }
}

