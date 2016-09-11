package jp.gr.java_conf.ya.yumura; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import jp.gr.java_conf.ya.yumura.Network.CheckConnectivity;
import jp.gr.java_conf.ya.yumura.Setting.PreferenceManage;
import jp.gr.java_conf.ya.yumura.String.IntentString;
import jp.gr.java_conf.ya.yumura.Twitter.KeyManage;
import jp.gr.java_conf.ya.yumura.Twitter.TwitterAccess;

public class ViewerActivity extends AppCompatActivity {

    public static String KEY_UPDATE_TEXT = "updateText";

    private static String GOOGLE_SEARCH_URL = "https://www.google.co.jp/search?q=";
    private static String BLANK_URL = "about:blank";

    private SearchView searchView;
    private String searchViewString = "";
    private String pref_webview_custom_useragent = "";
    private WebView webView;
    private boolean pref_debug_write_logcat = true;
    private boolean pref_webview_js_enabled = false;
    private boolean pref_webview_urlcheck_enabled = true;
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String searchWord) {
            return setSearchWord(searchWord);
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref_debug_write_logcat = PreferenceManage.getBoolean(this, "pref_debug_write_logcat", false);
        pref_webview_js_enabled = PreferenceManage.getBoolean(this, "pref_webview_js_enabled", false);
        pref_webview_urlcheck_enabled = PreferenceManage.getBoolean(this, "pref_webview_urlcheck_enabled", false);
        pref_webview_custom_useragent = PreferenceManage.getString(this, "pref_webview_custom_useragent", "");

        new Thread(new Runnable() {
            @Override
            public final void run() {
                final String urlString = IntentString.getUrlFromIntent(getIntent(), BLANK_URL);

                final boolean isFastWifi = CheckConnectivity.getWifiSpeed() >= 300;

                runOnUiThread(new Runnable() {
                    @SuppressLint("SetJavaScriptEnabled")
                    @Override
                    public final void run() {
                        webView = new WebView(ViewerActivity.this);
                        webView.setWebViewClient(new WebViewClient());
                        webView.getSettings().setAppCacheEnabled(true);
                        webView.getSettings().setCacheMode(
                                isFastWifi
                                        ? WebSettings.LOAD_NO_CACHE
                                        : WebSettings.LOAD_CACHE_ELSE_NETWORK);
                        webView.getSettings().setJavaScriptEnabled(pref_webview_js_enabled);
                        if (!pref_webview_custom_useragent.equals(""))
                            webView.getSettings().setUserAgentString(pref_webview_custom_useragent);
                        setContentView(webView);

                        loadUrl(urlString);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
        MenuItem menuItem = menu.findItem(R.id.search_menu_search);
        this.searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        this.searchView.setIconifiedByDefault(true);
        this.searchView.setSubmitButtonEnabled(true);
        this.searchView.setOnQueryTextListener(this.onQueryTextListener);

        if (this.searchViewString != null) {
            if (!this.searchViewString.equals("")) {
                this.searchView.setQuery(this.searchViewString, false);
            } else {
                String queryHint = getString(R.string.search_menu_search_hint);
                this.searchView.setQueryHint(queryHint);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_update) {
            new Thread(new Runnable() {
                @Override
                public final void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public final void run() {
                            try {
                                final String updateText = webView.getTitle() + " " + webView.getUrl();
                                final Bundle args = new Bundle();
                                args.putString(KEY_UPDATE_TEXT, updateText);
                                final DialogFragment_UpdateStatus dialogFragment_UpdateStatus = new DialogFragment_UpdateStatus();
                                dialogFragment_UpdateStatus.setArguments(args);
                                dialogFragment_UpdateStatus.show(getSupportFragmentManager(), getString(R.string.action_update));
                            } catch (Exception e) {
                                if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
                            }
                        }
                    });
                }
            }).start();


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkUrl(String urlString) {
        if (pref_webview_urlcheck_enabled) {
            if (urlString.equals(BLANK_URL)) {
                return true;
            }
            try {
                final URL url = new URL(urlString);
                final String protocol = url.getProtocol();
                if (protocol.equals("http") || protocol.equals("https")) {
                    return true;
                }
            } catch (MalformedURLException e) {
                if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
            }
            return false;
        } else {
            return true;
        }
    }

    private void loadUrl(String urlString) {
        if (checkUrl(urlString)) {
            webView.loadUrl(urlString);
        }
    }

    private boolean setSearchWord(final String searchWord) {
        new Thread(new Runnable() {
            @Override
            public final void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {
                        try {
                            ActionBar actionBar = getSupportActionBar();
                            actionBar.setTitle(searchWord);
                            actionBar.setDisplayShowTitleEnabled(true);
                            if (searchWord != null && !searchWord.equals("")) {
                                searchViewString = searchWord;
                                if ((searchViewString.startsWith("http")) || (searchViewString.startsWith("about:"))) {
                                    loadUrl(searchViewString);
                                } else {
                                    loadUrl(GOOGLE_SEARCH_URL + URLEncoder.encode(searchViewString, "UTF-8"));
                                }
                            }
                            searchView.clearFocus();
                            actionBar.collapseActionView();
                        } catch (Exception e) {
                            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
                        }
                    }
                });
            }
        }).start();

        return false;
    }

    public static class DialogFragment_UpdateStatus extends DialogFragment {
        private EditText editText;
        private TlAdapter adapter;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle bundle = getArguments();
            final String updateText = bundle.getString(KEY_UPDATE_TEXT);

            editText = new EditText(getActivity());
            editText.setHint(R.string.action_update_text);
            editText.setText(updateText);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.action_update);
            builder.setView(editText);
            builder.setPositiveButton(R.string.action_update,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if ((editText != null) && (!editText.getText().toString().equals(""))) {
                                adapter = new TlAdapter(getActivity(), null);
                                TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                twitterAccess.updateStatus(KeyManage.getCurrentUser().screenName, editText.getText().toString());
                            }
                        }
                    });
            return builder.create();
        }
    }
}
