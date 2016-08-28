package jp.gr.java_conf.ya.yumura; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import java.net.URLEncoder;
import java.util.Iterator;

import jp.gr.java_conf.ya.yumura.String.IntentString;
import jp.gr.java_conf.ya.yumura.Twitter.TwitterAccess;

public class ViewerActivity extends AppCompatActivity {

    private static String GOOGLE_SEARCH_URL = "https://www.google.co.jp/search?q=";

    private SearchView searchView;
    private String searchViewString;
    private WebView webView;
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

        new Thread(new Runnable() {
            @Override
            public final void run() {
                final String url = IntentString.getUrlFromIntent(getIntent(),"about:blank");

                runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {
                        webView = new WebView(ViewerActivity.this);
                        webView.setWebViewClient(new WebViewClient());
                        webView.loadUrl(url);
                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.getSettings().setUserAgentString("");
                        setContentView(webView);
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
                                args.putString("updateText", updateText);
                                final DialogFragment_UpdateStatus dialogFragment_UpdateStatus = new DialogFragment_UpdateStatus();
                                dialogFragment_UpdateStatus.setArguments(args);
                                dialogFragment_UpdateStatus.show(getSupportFragmentManager(), getString(R.string.action_update));
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            }).start();


            return true;
        }

        return super.onOptionsItemSelected(item);
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
                                if ((searchViewString.startsWith("http://")) || (searchViewString.startsWith("https://")) || (searchViewString.startsWith("about:"))) {
                                    webView.loadUrl(searchViewString);
                                } else {
                                    webView.loadUrl(GOOGLE_SEARCH_URL + URLEncoder.encode(searchViewString, "UTF-8"));
                                }
                            }
                            searchView.clearFocus();
                            actionBar.collapseActionView();
                        } catch (Exception e) {
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
            final String updateText = bundle.getString("updateText");

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
                                adapter = new TlAdapter(getActivity(),null);
                                TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                twitterAccess.updateStatus(editText.getText().toString());
                            }
                        }
                    });
            return builder.create();
        }
    }
}
