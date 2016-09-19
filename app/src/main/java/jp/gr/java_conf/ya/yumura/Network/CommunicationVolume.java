package jp.gr.java_conf.ya.yumura.Network; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.app.ActivityManager;
import android.content.Context;
import android.net.TrafficStats;

import jp.gr.java_conf.ya.yumura.App;

public class CommunicationVolume {
    public static long[] getCommunicationVolumeOfThisApp() {
        final ActivityManager am = (ActivityManager) App.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (final ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if ((App.getContext() != null) & (!App.getContext().getPackageName().equals(""))) {
                if (info.processName.equals(App.getContext().getPackageName())) {
                    long uidRxBytes = TrafficStats.getUidRxBytes(info.uid);
                    long uidTxBytes = TrafficStats.getUidTxBytes(info.uid);

                    return new long[]{uidTxBytes, uidRxBytes};
                }
            }
        }

        return new long[]{0L, 0L};
    }
}
