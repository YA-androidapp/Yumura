package jp.gr.java_conf.ya.yumura; // Copyright (c) 2013-2017 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import jp.gr.java_conf.ya.yumura.Twitter.KeyManage;
import jp.gr.java_conf.ya.yumura.Twitter.OAuthUser;
import jp.gr.java_conf.ya.yumura.Twitter.TwitterAccess;
import twitter4j.auth.AccessToken;

public class CallbackActivity extends Activity {
    private boolean pref_debug_write_logcat = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callback);

        final Intent intent = getIntent();
        if (intent != null) {
            final Uri uri = intent.getData();
            if (uri != null && uri.toString().startsWith(TwitterAccess.CALLBACK_URL)) {
                final String verifier = uri.getQueryParameter(TwitterAccess.CALLBACK_URL_VERIFIER);
                final String[] savedConsumerKeyAndSecret = KeyManage.loadCurrentConsumerKeyAndSecret();
                if (pref_debug_write_logcat)
                    Log.i("Yumura", "loadCurrentConsumerKeyAndSecret: " + savedConsumerKeyAndSecret[0] + " , " + savedConsumerKeyAndSecret[1]);

                new Thread(new Runnable() {
                    @Override
                    public final void run() {
                        try {
                            AccessToken accessToken = TlActivity.oAuthAuthorization.getOAuthAccessToken(TlActivity.requestToken, verifier);
                            KeyManage.addUser(new OAuthUser(accessToken.getScreenName(), savedConsumerKeyAndSecret[0], savedConsumerKeyAndSecret[1], accessToken.getScreenName(), accessToken.getToken(), accessToken.getTokenSecret(), accessToken.getUserId()));

                            final Intent i = new Intent(getApplicationContext(), TlActivity.class);
                            startActivity(i);
                        } catch (Exception e) {
                            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
                        }
                    }
                }).start();
            }
        }
    }
}