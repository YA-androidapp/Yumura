package jp.gr.java_conf.ya.yumura.Setting; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jp.gr.java_conf.ya.yumura.R;

public class PreferenceActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, new PreferenceActivityFragment()).commit();
    }
}

