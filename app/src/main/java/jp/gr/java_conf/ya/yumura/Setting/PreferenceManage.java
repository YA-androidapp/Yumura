package jp.gr.java_conf.ya.yumura.Setting; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferenceManage {
    public static final String Last_Read_Tweet = "lastReadTweet";
    private static boolean pref_debug_write_logcat = false;

    public static boolean getBoolean(final Context context, final String key, final boolean defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final boolean flo = preferences.getBoolean(key, defaultValue);

            return flo;
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }

        return false;
    }

    public static float getFloat(final Context context, final String key, final float defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String str = preferences.getString(key, Float.toString(defaultValue));
            final float flo = Float.parseFloat(str);

            return flo;
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }

        return 0f;
    }

    public static int getInt(final Context context, final String key, final int defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String str = preferences.getString(key, Integer.toString(defaultValue));
            final int inte = Integer.parseInt(str);

            return inte;
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }

        return 0;
    }

    public static long getLong(final Context context, final String key, final long defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String str = preferences.getString(key, Long.toString(defaultValue));
            final long lon = Long.parseLong(str);

            return lon;
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }

        return 0;
    }

    public static String getString(final Context context, final String key, final String defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String str = preferences.getString(key, defaultValue);

            return str;
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }

        return "";
    }

    public static boolean putLong(final Context context, final String key, final long value) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, Long.toString(value));
            return editor.commit();
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }

        return false;
    }

    public static boolean putString(final Context context, final String key, final String defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, defaultValue);
            return editor.commit();
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }

        return false;
    }
}
