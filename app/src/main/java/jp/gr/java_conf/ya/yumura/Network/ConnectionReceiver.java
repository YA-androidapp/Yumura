package jp.gr.java_conf.ya.yumura.Network; // Copyright (c) 2013-2017 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionReceiver extends BroadcastReceiver {
    private Observer mObserver;

    public ConnectionReceiver(Observer observer) {
        mObserver = observer;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if ((activeNetwork.isConnectedOrConnecting()) || (activeNetwork.isConnected())) {
                try {
                    if (mObserver != null)
                        mObserver.onConnect();
                } catch (Exception e) {
                }
                return;
            }
        }
        try {
            if (mObserver != null)
                mObserver.onDisconnect();
        } catch (Exception e) {
        }
    }

    public interface Observer {
        void onConnect();

        void onDisconnect();
    }
}