package jp.gr.java_conf.ya.yumura; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import jp.gr.java_conf.ya.yumura.Layout.EndlessScrollListener;
import jp.gr.java_conf.ya.yumura.Layout.LinearLayoutManagerWithSmoothScroller;
import jp.gr.java_conf.ya.yumura.Network.CheckConnectivity;
import jp.gr.java_conf.ya.yumura.Setting.PreferenceActivity;
import jp.gr.java_conf.ya.yumura.Setting.PreferenceManage;
import jp.gr.java_conf.ya.yumura.String.IntentString;
import jp.gr.java_conf.ya.yumura.Twitter.KeyManage;
import jp.gr.java_conf.ya.yumura.Twitter.TwitterAccess;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;

public class TlActivity extends AppCompatActivity {
    public static OAuthAuthorization oAuthAuthorization;
    public static RequestToken requestToken;

    private int pref_tl_api_count = 200;

    private TlAdapter adapter;
    private TwitterAccess twitterAccess;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;

    private SearchView searchView;
    private String searchViewString = "";
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

    public static void showSnackbar(final String text, final String actionText) {
        final FloatingActionButton fab = (FloatingActionButton) ((Activity) App.getContext()).findViewById(R.id.fab);
        Snackbar.make(fab, text, Snackbar.LENGTH_LONG)
                .setAction(actionText, null).show();
    }

    private void doOAuth(final String consumerKey, final String consumerSecret) {
        final String currentConsumerKey = (!consumerKey.equals("")) ? consumerKey : TwitterAccess.CONSUMER_KEY;
        final String currentConsumerSecret = (!consumerSecret.equals("")) ? consumerSecret : TwitterAccess.CONSUMER_SECRET;

        KeyManage.saveCurrentConsumerKeyAndSecret(currentConsumerKey, currentConsumerSecret);

        try {
            oAuthAuthorization = new OAuthAuthorization(TwitterAccess.getConfiguration());
            oAuthAuthorization.setOAuthConsumer(currentConsumerKey, currentConsumerSecret);

            new Thread(new Runnable() {
                @Override
                public final void run() {
                    try {
                        requestToken = oAuthAuthorization.getOAuthRequestToken(TwitterAccess.CALLBACK_URL);
                        startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthorizationURL())), 0);
                    } catch (Exception e) {
//                        Log.v("Yumura", e.getMessage());
                    }
                }
            }).start();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.v("Yumura", "onCreate()");
        super.onCreate(savedInstanceState);

        setViews();

        if (CheckConnectivity.isConnected()) {
//            Log.v("Yumura", "onCreate() (CheckConnectivity.isConnected())");
            if (KeyManage.getUserCount() < 1) {
//                Log.v("Yumura", "onCreate() (KeyManage.getUserCount() < 1)");
                (new DialogFragment_ChangeOAuthKey()).show(getSupportFragmentManager(), getString(R.string.action_change_consumer_key));
            } else {
//                Log.v("Yumura", "onCreate() !(KeyManage.getUserCount() < 1)");
                pref_tl_api_count = PreferenceManage.getInt(this, "pref_tl_api_count", 200);

                final String url = IntentString.getUrlFromIntent(getIntent(), TwitterAccess.URL_PROTOCOL + TwitterAccess.URL_TWITTER);

//                Log.v("Yumura", "onCreate() url: "+url);
                setSearchWord(url);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tl, menu);
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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferenceActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_makeShortcut) {
            makeShortcut(searchViewString);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean setSearchWord(final String searchWord) {
//        Log.v("Yumura", "setSearchWord("+searchWord+")");
        try {
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(searchWord);
            actionBar.setDisplayShowTitleEnabled(true);
            if (searchWord != null && !searchWord.equals("")) {
                if (CheckConnectivity.isConnected()) {
//                            Log.v("Yumura", "(CheckConnectivity.isConnected())");
                    if (searchViewString.equals(searchWord)) {
//                                Log.v("Yumura", "(searchViewString.equals(searchWord))");
//                                Log.v("Yumura", "searchWord: " + searchWord);
                        if (KeyManage.getUserCount() > 0) {
                            twitterAccess = new TwitterAccess(adapter);
                            try {
                                twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, adapter.getList().get(0).getId() + 1);
                            } catch (Exception e) {
                                twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, -1);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } else {
//                                Log.v("Yumura", "!(searchViewString.equals(searchWord))");
                        searchViewString = searchWord;
//                                Log.v("Yumura", "searchWord: " + searchWord);
                        if (KeyManage.getUserCount() > 0) {
                            adapter.clearData();
                            adapter.notifyDataSetChanged();
                            twitterAccess = new TwitterAccess(adapter);
                            twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, -1);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    changeRefreshLayoutIcon(false);
                }
            }
            if (searchView != null)
                searchView.clearFocus();
            actionBar.collapseActionView();
        } catch (Exception e) {
        }

        return false;
    }

    private void setViews() {
        setContentView(R.layout.activity_tl);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (new DialogFragment_UpdateStatus()).show(getSupportFragmentManager(), getString(R.string.action_update));
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new TlAdapter(this, recyclerView);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                if (!swipeRefresh.isRefreshing()) {
                if (twitterAccess == null)
                    twitterAccess = new TwitterAccess(adapter);
                try {
                    twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, adapter.getList().get(0).getId() + 1);
                } catch (Exception e) {
                    twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, -1);
                }
//                }

                changeRefreshLayoutIcon(false);
            }
        });

        setRecyclerView();
    }

    private final Intent uriStringToIntent(final String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("jp.gr.java_conf.ya.yumura", "jp.gr.java_conf.ya.yumura.TlActivity");
        intent.setData(Uri.parse(url));

        return intent;
    }

    private final String uriStringToShortcutName(final String url) {
        String result = url;
        result = result.replaceAll(TwitterAccess.URL_PROTOCOL + TwitterAccess.URL_TWITTER, "");
        result = result.replaceAll("/lists/", "/");

        if (result.endsWith("/"))
            result = result.substring(0, result.length() - 2);

        if (result.equals(""))
            result = "home";

        return result;
    }

    private final void makeShortcut(final String url) {
        final Intent shortcutIntent = uriStringToIntent(url);

        final Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        final Parcelable icon = Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, uriStringToShortcutName(url));
        this.sendBroadcast(intent);
    }

    private void changeRefreshLayoutIcon(boolean enable) {
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        if (enable) {
//            if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(true);
//            }
        } else {
            new Thread(new Runnable() {
                @Override
                public final void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public final void run() {
                            swipeRefresh.setRefreshing(false);
                        }
                    });
                }
            }).start();
        }
    }

    private void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new EndlessScrollListener((LinearLayoutManager) recyclerView.getLayoutManager()) {
            @Override
            public void onLoadMore(int page) {
                swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
                if (!swipeRefresh.isRefreshing()) {
                    changeRefreshLayoutIcon(true);

                    if (twitterAccess == null)
                        twitterAccess = new TwitterAccess(adapter);
                    try {
                        twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, adapter.getList().get(adapter.getItemCount() - 1).getId() - 1, -1, -1);
                    } catch (Exception e) {
                        twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, -1);
                    }

                    changeRefreshLayoutIcon(false);
                }
            }
        });
    }

    public static class DialogFragment_ChangeOAuthKey extends DialogFragment {
        private EditText editText;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            editText = new EditText(getActivity());
            editText.setText("");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.action_change_consumer_key);
            builder.setMessage(R.string.message_change_consumer_key);
            builder.setView(editText);
            builder.setPositiveButton(R.string.message_change_consumer_key_yes,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final TlActivity activity = (TlActivity) getActivity();

                            if ((editText != null) && (!editText.getText().toString().equals(""))) {
                                final String[] consumer = editText.getText().toString().trim().split("\\s");
                                if (consumer.length == 2) {
                                    activity.doOAuth(consumer[0], consumer[1]);
                                } else {
                                    activity.doOAuth("", "");
                                }
                            } else {
                                activity.doOAuth("", "");
                            }
                        }
                    });
            builder.setNegativeButton(R.string.message_change_consumer_key_no,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final TlActivity activity = (TlActivity) getActivity();
                            activity.doOAuth(TwitterAccess.CONSUMER_KEY, TwitterAccess.CONSUMER_SECRET);
                        }
                    });

            return builder.create();
        }
    }

    public static class DialogFragment_UpdateStatus extends DialogFragment {
        private EditText editText;
        private TlAdapter adapter;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            editText = new EditText(getActivity());
            editText.setHint(R.string.action_update_text);
            editText.setText("");

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
                                twitterAccess.updateStatus(editText.getText().toString());
                            }
                        }
                    });
            return builder.create();
        }
    }
}
