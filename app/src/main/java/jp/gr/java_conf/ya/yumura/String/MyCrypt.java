package jp.gr.java_conf.ya.yumura.String; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import jp.gr.java_conf.ya.yumura.App;
import jp.gr.java_conf.ya.yumura.R;

public final class MyCrypt {
    // TODO private
    public static final String getCrpKey() {
        String crpKey = App.getResString(R.string.app_name);
        try {
            final PackageInfo packageInfo = App.getContext().getPackageManager().getPackageInfo("jp.gr.java_conf.ya.yumura", PackageManager.GET_META_DATA);
            crpKey += Long.toString(packageInfo.firstInstallTime);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return crpKey;
    }

    public static final String decrypt(final String encrypted_str) {
        try {
            final byte[] decrypted = Base64.decode(encrypted_str, Base64.DEFAULT);
            final SecretKeySpec sksSpec = new SecretKeySpec(getCrpKey().getBytes(), "Blowfish");
            final Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, sksSpec);
            return new String(cipher.doFinal(decrypted));
        } catch (final Exception e) {
        }
        return "";
    }

    @SuppressLint("TrulyRandom")
    public static final String encrypt(final String text) {
        try {
            final SecretKeySpec sksSpec = new SecretKeySpec(getCrpKey().getBytes(), "Blowfish");
            final Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, sksSpec);
            final byte[] encrypted = cipher.doFinal(text.getBytes());
            final String encrypted_str = Base64.encodeToString(encrypted, Base64.DEFAULT);
            return encrypted_str;
        } catch (final Exception e) {
        }
        return "";
    }
}

