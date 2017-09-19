package jp.gr.java_conf.ya.yumura.Setting; // Copyright (c) 2013-2017 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import jp.gr.java_conf.ya.yumura.R;

public class PreferenceActivityFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_yumura);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        resetSummary();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        resetSummary();
    }

    public void resetSummary() {
        final PreferenceScreen screen = this.getPreferenceScreen();
        if ((screen != null) && (screen.getPreferenceCount() > 0)) {
            for (int i = 0; i < screen.getPreferenceCount(); i++) {
                try {
                    final Preference pref = screen.getPreference(i);
                    if (pref != null) {
                        if (pref instanceof EditTextPreference) {
                            try {
                                final String val = ((EditTextPreference) pref).getText();
                                if (val != null)
                                    pref.setSummary(val);
                            } catch (Exception e) {
                            }
                        } else if (pref instanceof ListPreference) {
                            try {
                                final String val = ((ListPreference) pref).getEntry().toString();
                                if (val != null)
                                    pref.setSummary(val);
                            } catch (Exception e) {
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }
}