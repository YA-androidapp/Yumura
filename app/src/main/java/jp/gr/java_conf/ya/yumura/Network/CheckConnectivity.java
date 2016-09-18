package jp.gr.java_conf.ya.yumura.Network; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import jp.gr.java_conf.ya.yumura.App;

public class CheckConnectivity {

    public static boolean isConnected() {
        final Context context = App.getContext();
        if (context != null) {
            final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null)
                return activeNetwork.isConnectedOrConnecting() || activeNetwork.isConnected();
        }
        return false;
    }

    public static String getConnectedType() {
        final Context context = App.getContext();
        if (context != null) {
            final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo info = manager.getActiveNetworkInfo();
            if ((info != null) && (info.isConnected()) || (info.isConnectedOrConnecting())) {
                switch (info.getType()) {
                    case ConnectivityManager.TYPE_WIFI:         // Wifi
                        return "WIFI";
                    case ConnectivityManager.TYPE_MOBILE_DUN:   // Mobile 3G
                    case ConnectivityManager.TYPE_MOBILE:
                        switch (info.getSubtype()) {
                            case TelephonyManager.NETWORK_TYPE_LTE:
                                return "4G";
                            default:
                                return "3G";
                        }
                    case ConnectivityManager.TYPE_WIMAX:        // Wimax
                        return "WIMAX";
                }
            }
        }
        return "";
    }

    public static int getWifiSpeed() {
        final Context context = App.getContext();
        if (context != null) {
            if (getConnectedType().equals("WIFI")) {
                final WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                final WifiInfo info = manager.getConnectionInfo();

                // String.format("SSID : %s", info.getSSID());
                // int ipAdr = info.getIpAddress();
                // String.format("IP Adrress : %02d.%02d.%02d.%02d",
                //            (ipAdr>>0)&0xff, (ipAdr>>8)&0xff, (ipAdr>>16)&0xff, (ipAdr>>24)&0xff);
                // String.format("MAC Address : %s", info.getMacAddress());
                // int rssi = info.getRssi();

                return info.getLinkSpeed();
            } else if (!getConnectedType().equals("")) {
                return 0;
            }
        }
        return -1;
    }
}
