package jp.gr.java_conf.ya.yumura.Network; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.app.ActivityManager;
import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;

import java.util.Locale;

import jp.gr.java_conf.ya.yumura.App;

public class CommunicationVolume {
    private static boolean pref_debug_write_logcat = true;

    public static long[] getCommunicationVolumeArrayOfThisApp() {
        if (pref_debug_write_logcat) Log.i("Yumura", "long[] getCommunicationVolumeArrayOfThisApp()");
        final ActivityManager am = (ActivityManager) App.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (final ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if ((App.getContext() != null) & (!App.getContext().getPackageName().equals(""))) {
                if (info.processName.equals(App.getContext().getPackageName())) {
                    if (pref_debug_write_logcat)
                        Log.i("Yumura", "getCommunicationVolumeArrayOfThisApp() info:" + info.processName);

                    final long uidRxBytes = TrafficStats.getUidRxBytes(info.uid);
                    final long uidTxBytes = TrafficStats.getUidTxBytes(info.uid);
                    return new long[]{uidTxBytes, uidRxBytes};
                }
            }
        }

        return new long[]{-1L, -1L};
    }
}
