package jp.gr.java_conf.ya.yumura; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.ViewGroup;

import java.io.File;
import java.util.UUID;

import jp.gr.java_conf.ya.yumura.String.ViewString;
import twitter4j.Status;
import twitter4j.TwitterException;

public class App extends android.app.Application {

    public static final String TAG = "Yumura";

    private static App instance;
    public App() {
        instance = this;
    }
    public static App getInstance() {
        return instance;
    }

    public static Context getContext(){
        try {
            return getInstance().getApplicationContext();
        }catch (Exception e){
            return null;
        }
    }

    public static String getResString(final int resId){
        try {
            return getContext().getString(resId);
        }catch (Exception e){
            return null;
        }
    }

    public static void Log(Object obj, String str) {
        android.util.Log.d(App.TAG,
                String.format("@%s \n %s", obj.toString(), str));
    }

    public static void Log(Object obj, Exception e) {
        e.printStackTrace();
        android.util.Log.e(App.TAG,
                String.format("@%s \n %s", obj.toString(), e.toString()));
    }

    public static void Log(Object obj, Status status, TwitterException e) {
        e.printStackTrace();
        android.util.Log.e(App.TAG,
                String.format("@%s \n %s \n %s", obj.toString(), ViewString.getScreennameAndText(status), e.toString()));
    }

    public int dp2px(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public int px2dp(int px) {
        return (int) (px / getResources().getDisplayMetrics().density);
    }
}