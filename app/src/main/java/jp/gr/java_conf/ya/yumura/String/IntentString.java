package jp.gr.java_conf.ya.yumura.String; // Copyright (c) 2013-2017 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.content.Intent;
import android.os.Bundle;

import java.util.Iterator;

public class IntentString {
    public static String getUrlFromIntent(final Intent intent, final String defaultUrl) {
        String url = defaultUrl;

        if ((intent != null) && (intent.getAction() != null)) {
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                final String str = intent.getDataString();
                if (str != null) {
                    url = str;
                } else {
                    final Bundle args = intent.getExtras();
                    if ((args != null) && (args.keySet() != null)) {
                        Iterator<?> it = args.keySet().iterator();
                        final StringBuffer sb = new StringBuffer();
                        while (it.hasNext()) {
                            final String key = (String) it.next();
                            final String value = args.getString(key);
                            if (value != null) {
                                sb.append(value);
                            }
                        }
                        url = sb.toString();
                    }
                }
            } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
                final Bundle extras = intent.getExtras();
                if (extras != null) {
                    final CharSequence ext = extras.getCharSequence(Intent.EXTRA_TEXT);
                    if (ext != null) {
                        url = ext.toString();
                    }
                }
            }
        }
        return url;
    }
}