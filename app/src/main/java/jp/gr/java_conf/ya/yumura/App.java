package jp.gr.java_conf.ya.yumura; // Copyright (c) 2013-2017 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.content.Context;

public class App extends android.app.Application {
    private static App instance;

    public App() {
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    public static Context getContext() {
        try {
            return getInstance().getApplicationContext();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getResString(final int resId) {
        try {
            return getContext().getString(resId);
        } catch (Exception e) {
            return null;
        }
    }

    public int dp2px(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public int px2dp(int px) {
        return (int) (px / getResources().getDisplayMetrics().density);
    }
}