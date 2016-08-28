package jp.gr.java_conf.ya.yumura.Setting; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceManage {
    public static boolean getBoolean(final Context context, final String key, final boolean defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String str = preferences.getString(key,Boolean.toString(defaultValue));
            final boolean flo = Boolean.parseBoolean(str);

            return flo;
        } catch (Exception e) {
        }

        return false;
    }

    public static float getFloat(final Context context, final String key, final float defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String str = preferences.getString(key,Float.toString(defaultValue));
            final float flo = Float.parseFloat(str);

            return flo;
        } catch (Exception e) {
        }

        return 0f;
    }

    public static int getInt(final Context context, final String key, final int defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String str = preferences.getString(key,Integer.toString(defaultValue));
            final int inte = Integer.parseInt(str);

            return inte;
        } catch (Exception e) {
        }

        return 0;
    }

    public static String getString(final Context context, final String key, final String defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String str = preferences.getString(key,defaultValue);

            return str;
        } catch (Exception e) {
        }

        return "";
    }
}
