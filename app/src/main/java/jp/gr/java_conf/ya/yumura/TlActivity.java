package jp.gr.java_conf.ya.yumura; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jp.gr.java_conf.ya.yumura.Layout.EndlessScrollListener;
import jp.gr.java_conf.ya.yumura.Layout.LinearLayoutManagerWithSmoothScroller;
import jp.gr.java_conf.ya.yumura.Network.CheckConnectivity;
import jp.gr.java_conf.ya.yumura.Network.ConnectionReceiver;
import jp.gr.java_conf.ya.yumura.Setting.PreferenceActivity;
import jp.gr.java_conf.ya.yumura.Setting.PreferenceManage;
import jp.gr.java_conf.ya.yumura.String.IntentString;
import jp.gr.java_conf.ya.yumura.String.SearchStatus;
import jp.gr.java_conf.ya.yumura.String.ViewString;
import jp.gr.java_conf.ya.yumura.Time.Cal;
import jp.gr.java_conf.ya.yumura.Time.Time;
import jp.gr.java_conf.ya.yumura.Twitter.BinarySearchUtil;
import jp.gr.java_conf.ya.yumura.Twitter.KeyManage;
import jp.gr.java_conf.ya.yumura.Twitter.OAuthUser;
import jp.gr.java_conf.ya.yumura.Twitter.TwitterAccess;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;

public class TlActivity extends AppCompatActivity implements ConnectionReceiver.Observer {
    public static OAuthAuthorization oAuthAuthorization;

    public static RequestToken requestToken;

    public static String KEY_ENTER_URL = "EnterUrl";
    public static String KEY_URL_ITEMS = "UrlItems";
    private static String[] autoCompleteItems_EnterUrl;
    private static Date preGetAutoCompleteItems_EnterUrl = new Date(0);
    private boolean isConnected = false;
    private boolean pref_debug_write_logcat = false;
    private ConnectionReceiver receiver;
    private Date preOnLoadMoreTime = new Date(0);
    private Date preSwipeRefreshTime = new Date(0);
    private FloatingActionButton fab;
    private int pref_tl_api_count = 200;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private SearchView searchView;
    private String searchViewString = "";

    private TlAdapter adapter;
    private TwitterAccess twitterAccess;
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
    private int sequenceLoadedNumber = 0;
    private int maxSequenceLoadedNumber = 3;

    public static String[] getAutoCompleteItems_EnterUrl() {
        if ((autoCompleteItems_EnterUrl != null)
                && (autoCompleteItems_EnterUrl.length > 0)
                && (Time.differenceMinutes(preGetAutoCompleteItems_EnterUrl) > 10)) {
            preGetAutoCompleteItems_EnterUrl = new Date();
            return autoCompleteItems_EnterUrl;
        } else {
            List<String> items = new ArrayList<>();
            for (OAuthUser oAuthUser : KeyManage.getUsers()) {
                // ホームTL
                items.add(TwitterAccess.URL_PROTOCOL + TwitterAccess.URL_TWITTER
                        + "#" + oAuthUser.screenName);

                // リプライ
                items.add(TwitterAccess.URL_PROTOCOL + TwitterAccess.URL_TWITTER
                        + "/" + TwitterAccess.URL_TWITTER_MENTION + "#" + oAuthUser.screenName);

                // ユーザTL
                items.add(TwitterAccess.URL_PROTOCOL + TwitterAccess.URL_TWITTER
                        + "/" + oAuthUser.screenName);

                // ふぁぼ
                items.add(TwitterAccess.URL_PROTOCOL + TwitterAccess.URL_TWITTER
                        + "/" + TwitterAccess.URL_TWITTER_FAVORITE_1 + "/" + TwitterAccess.URL_TWITTER_FAVORITE_2
                        + "#" + oAuthUser.screenName);

                items.add(TwitterAccess.URL_PROTOCOL + TwitterAccess.URL_TWITTER_SEARCH);

                for (final String listUrl : TwitterAccess.getUserListUrls(oAuthUser.screenName))
                    items.add(listUrl);
            }
            Collections.sort(items);

            autoCompleteItems_EnterUrl = items.toArray(new String[0]);
            return autoCompleteItems_EnterUrl;
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

        if (id == R.id.action_enter_url) {
            enterUrl();
            return true;
        } else if (id == R.id.action_delJustBefore) {
            delJustBefore();
            return true;
        } else if (id == R.id.action_load_upper) {
            loadTimelineUpper(true);
            return true;
        } else if (id == R.id.action_load_lower) {
            loadTimelineUpper(false);
            return true;
        } else if (id == R.id.action_makeShortcut) {
            makeShortcut(searchViewString);
            return true;
        } else if (id == R.id.action_move_to_bottom) {
            adapter.scrollTo(adapter.getItemCount() - 2);
            return true;
        } else if (id == R.id.action_move_to_top) {
            adapter.scrollTo(0);
            return true;
        } else if (id == R.id.action_move_to_unread) {

            new Thread(new Runnable() {
                @Override
                public final void run() {
                    adapter.moveToUnread();
                }
            }).start();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferenceActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_tl_search) {
            (new DialogFragment_TlSearch()).show(getSupportFragmentManager(), getString(R.string.action_tl_search));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnect() {
        isConnected = true;
        // Toast.makeText(this, getString(R.string.message_on_connect), Toast.LENGTH_SHORT).show();

        if (fab == null)
            fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(android.R.drawable.ic_menu_edit);
    }

    private void getPreferences() {
        pref_debug_write_logcat = PreferenceManage.getBoolean(this, "pref_debug_write_logcat", pref_debug_write_logcat);
        pref_tl_api_count = PreferenceManage.getInt(this, "pref_tl_api_count", pref_tl_api_count);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferences();

        registerConnectionReceiver();

        setViews();

        if (CheckConnectivity.isConnected()) {
            isConnected = true;

            if (KeyManage.getUserCount() < 1) {
                (new DialogFragment_ChangeOAuthKey()).show(getSupportFragmentManager(), getString(R.string.action_change_consumer_key));
            } else {
                pref_tl_api_count = PreferenceManage.getInt(this, "pref_tl_api_count", 200);
                final String url = IntentString.getUrlFromIntent(getIntent(), TwitterAccess.URL_PROTOCOL + TwitterAccess.URL_TWITTER);
                setSearchWord(url);
            }
        }
    }

    @Override
    protected void onPause() {
        unregisterConnectionReceiver();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterConnectionReceiver();

        super.onDestroy();
    }

    @Override
    public void onDisconnect() {
        isConnected = false;
        Toast.makeText(this, getString(R.string.message_on_disconnect), Toast.LENGTH_SHORT).show();

        if (fab == null)
            fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getPreferences();

        registerConnectionReceiver();
    }

    private final void changeRefreshLayoutIcon(boolean enable) {
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        if (enable) {
            swipeRefresh.setRefreshing(true);
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

    private final void delJustBefore() {
        new Thread(new Runnable() {
            @Override
            public final void run() {
                final Status status = TwitterAccess.getStatusesJustBefore(KeyManage.getCurrentUser().screenName).get(0);
                if (pref_debug_write_logcat)
                    Log.i("Yumura", "delJustBefore() " + (new ViewString()).getStatusText(status, false, "", ""));

                runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {

                        AlertDialog.Builder builder = new AlertDialog.Builder(TlActivity.this);
                        builder.setTitle(R.string.action_delJustBefore);
                        builder.setMessage(ViewString.getScreennameAndTextAndFooter(status));
                        builder.setPositiveButton(R.string.action_delJustBefore_del,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public final void run() {
                                                // adapter = new TlAdapter(TlActivity.this, recyclerView);
                                                TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                                twitterAccess.destroyStatus(KeyManage.getCurrentUser().screenName, status);
                                            }
                                        }).start();
                                    }
                                });
                        builder.create().show();
                    }
                });
            }
        }).start();
    }

    private final void doOAuth(final String consumerKey, final String consumerSecret) {
        final String currentConsumerKey = (!consumerKey.equals("")) ? consumerKey : TwitterAccess.CONSUMER_KEY;
        final String currentConsumerSecret = (!consumerSecret.equals("")) ? consumerSecret : TwitterAccess.CONSUMER_SECRET;

        KeyManage.saveCurrentConsumerKeyAndSecret(currentConsumerKey, currentConsumerSecret);

        try {
            oAuthAuthorization = new OAuthAuthorization(TwitterAccess.getConfiguration(currentConsumerKey, currentConsumerSecret));
            oAuthAuthorization.setOAuthConsumer(currentConsumerKey, currentConsumerSecret);

            new Thread(new Runnable() {
                @Override
                public final void run() {
                    try {
                        requestToken = oAuthAuthorization.getOAuthRequestToken(TwitterAccess.CALLBACK_URL);
                        startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthorizationURL())), 0);
                    } catch (Exception e1) {
                        if (pref_debug_write_logcat)
                            Log.e("Yumura", "doOAuth() E1: " + e1.getMessage());
                    }
                }
            }).start();
        } catch (Exception e2) {
            if (pref_debug_write_logcat) Log.e("Yumura", "doOAuth() E2: " + e2.getMessage());
        }
    }

    private final void enterUrl() {
        try {
            new Thread(new Runnable() {
                @Override
                public final void run() {
                    final Bundle args = new Bundle();
                    args.putString(KEY_ENTER_URL, searchViewString);
                    args.putStringArray(KEY_URL_ITEMS, getAutoCompleteItems_EnterUrl());
                    final DialogFragment_EnterUrl dialogFragment_EnterUrl = new DialogFragment_EnterUrl();
                    dialogFragment_EnterUrl.setArguments(args);

                    runOnUiThread(new Runnable() {
                        @Override
                        public final void run() {
                            dialogFragment_EnterUrl.show(getSupportFragmentManager(), getString(R.string.action_enter_url));
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", "enterUrl() E: " + e.getMessage());
        }
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

    private final void registerConnectionReceiver() {
        if (receiver == null) {
            try {
                receiver = new ConnectionReceiver(this);
                if (receiver != null) {
                    final IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                    try {
                        registerReceiver(receiver, intentFilter);
                    } catch (Exception e) {
                        unregisterConnectionReceiver();
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private final void unregisterConnectionReceiver() {
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
                receiver = null;
            } catch (Exception e) {
            }
        }
    }

    private final void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new EndlessScrollListener((LinearLayoutManager) recyclerView.getLayoutManager()) {
            @Override
            public void onLoadMore(int page) {
                loadTimelineUpper(false);

                changeRefreshLayoutIcon(false);
            }
        });
    }

    private final boolean setSearchWord(final String searchWord) {
        if (pref_debug_write_logcat) Log.i("Yumura", "setSearchWord(" + searchWord + ")");
        try {
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(searchWord);
            actionBar.setDisplayShowTitleEnabled(true);
            if (searchWord != null && !searchWord.equals("")) {
                if (isConnected) {
                    if (searchViewString.equals(searchWord)) {
                        if (pref_debug_write_logcat)
                            Log.i("Yumura", "(searchViewString.equals(" + searchWord + "))");
                        if (KeyManage.getUserCount() > 0) {
                            twitterAccess = new TwitterAccess(adapter);
                            if (adapter.getItemCount() < 1) {
                                twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, -1);
                            } else {
                                try {
                                    twitterAccess.loadTimeline(searchViewString, pref_tl_api_count,
                                            -1, -1, adapter.getList().get(0).getId() + 1);
                                } catch (Exception e) {
                                    twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, -1);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        if (pref_debug_write_logcat)
                            Log.i("Yumura", "!(searchViewString.equals(" + searchWord + "))");
                        searchViewString = searchWord;
                        if (KeyManage.getUserCount() > 0) {
                            if (pref_debug_write_logcat)
                                Log.i("Yumura", "(KeyManage.getUserCount() > 0)");
                            adapter.clearData();
                            adapter.notifyDataSetChanged();

                            twitterAccess = new TwitterAccess(adapter);
                            twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, -1);
                            adapter.notifyDataSetChanged();
                            if (pref_debug_write_logcat) Log.i("Yumura", "notifyDataSetChanged()");
                        }
                    }
                    changeRefreshLayoutIcon(false);
                }
            }
            if (searchView != null)
                searchView.clearFocus();
            actionBar.collapseActionView();
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", "setSearchWord() E: " + e.getMessage());
        }

        return false;
    }

    private final void setViews() {
        setContentView(R.layout.activity_tl);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
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
                if (pref_debug_write_logcat) Log.i("Yumura", "onRefresh()");
                loadTimelineUpper(true);
                changeRefreshLayoutIcon(false);
            }
        });

        setRecyclerView();
    }

    private void loadTimelineUpper(boolean upper) {
        if (!upper)
            changeRefreshLayoutIcon(true);

        if ((upper && (Time.differenceMinutes(preSwipeRefreshTime) > 0))
                || (!upper && (Time.differenceMinutes(preOnLoadMoreTime) > 0))) {
            sequenceLoadedNumber = 0;
        } else {
            sequenceLoadedNumber++;
        }
        if (sequenceLoadedNumber < maxSequenceLoadedNumber) {
            if (twitterAccess == null)
                twitterAccess = new TwitterAccess(adapter);

            if (adapter.getItemCount() < 1) {
                twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, -1);
            } else {
                try {
                    if (pref_debug_write_logcat)
                        Log.i("Yumura", "loadTimeline(" + searchViewString + ", " + Integer.toString(pref_tl_api_count)
                                + ", -1, -1, " + Long.toString(adapter.getList().get(0).getId() + 1) + ")");

                    PreferenceManage.putLong(TlActivity.this, PreferenceManage.Last_Read_Tweet, adapter.getList().get(0).getId());

                    twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, // url, count
                            (upper ? -1 : (adapter.getList().get(adapter.getItemCount() - 1).getId())), // maxId
                            -1, // page
                            (upper ? (adapter.getList().get(0).getId() + 1) : -1)); // sinceId
                } catch (Exception e) {
                    if (pref_debug_write_logcat)
                        Log.e("Yumura", "loadTimelineUpper() E: " + e.getMessage());
                    twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, -1);
                }
            }

            if (upper) {
                preSwipeRefreshTime = new Date();
            } else {
                preOnLoadMoreTime = new Date();
            }
        }
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

    public static class DialogFragment_EnterUrl extends DialogFragment {
        private AutoCompleteTextView editText;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle bundle = getArguments();
            final String urlString = bundle.getString(KEY_ENTER_URL);
            final String[] urlItems = bundle.getStringArray(KEY_URL_ITEMS);

            editText = new AutoCompleteTextView(getActivity());

            if ((autoCompleteItems_EnterUrl != null) && (autoCompleteItems_EnterUrl.length > 0)) {
                ArrayAdapter<String> stringArrayAdapter
                        = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, urlItems);
                editText.setAdapter(stringArrayAdapter);
            }
            editText.setHint(R.string.action_enter_url);
            editText.setText(urlString);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.action_enter_url);
            builder.setView(editText);
            builder.setPositiveButton(R.string.action_enter_url,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if ((editText != null) && (!editText.getText().toString().equals(""))) {
                                final TlActivity activity = (TlActivity) getActivity();
                                activity.searchView.setQuery(editText.getText().toString(), true);
                                activity.searchViewString = editText.getText().toString();
                            }
                        }
                    });
            return builder.create();
        }
    }

    public static class DialogFragment_UpdateStatus extends DialogFragment {
        private boolean pref_debug_write_logcat = true;
        private EditText editText;
        private TlAdapter adapter;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            editText = new EditText(getActivity());
            editText.setText("");
            editText.setHint(R.string.action_update_text);

            final String[] menuItemArray = KeyManage.getScreenNames("@", "");
            final AlertDialog.Builder accountDlg = new AlertDialog.Builder(getActivity());
            accountDlg.setTitle(getString(R.string.action_update_text));
            accountDlg.setView(editText);
            accountDlg.setItems(
                    menuItemArray,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if ((editText != null) && (!editText.getText().toString().equals(""))) {
                                try {
                                    adapter = ((TlActivity) getActivity()).adapter;
                                    final TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                    final StatusUpdate statusUpdate = new StatusUpdate(editText.getText().toString());
                                    twitterAccess.updateStatus(menuItemArray[which].replace("@", ""), statusUpdate);
                                } catch (Exception e) {
                                    if (pref_debug_write_logcat)
                                        Log.e("Yumura", "onCreateDialog() E: " + e.getMessage());
                                }
                            }
                        }
                    });
            return accountDlg.create();
        }
    }

    public static class DialogFragment_TlSearch extends DialogFragment {
        private EditText editText;
        private TlAdapter adapter;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            editText = new EditText(getActivity());
            editText.setText("");
            editText.setHint(R.string.action_tl_search);

            final String[] menuItemArray = { // ミュート種別
                    getString(R.string.action_tl_search_screenname),    // ユーザ名
                    getString(R.string.action_tl_search_text),          // 本文
                    getString(R.string.action_tl_search_createat),      // 日時
                    getString(R.string.action_tl_search_source)         // クライアント名
            };

            final AlertDialog.Builder accountDlg = new AlertDialog.Builder(getActivity());
            accountDlg.setTitle(getString(R.string.action_tl_search));
            accountDlg.setView(editText);
            accountDlg.setItems(
                    menuItemArray,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if ((editText != null) && (!editText.getText().toString().equals(""))) {
                                adapter = ((TlActivity) getActivity()).adapter;

                                if (menuItemArray[which].equals(getString(R.string.action_tl_search_screenname))) {
                                    final int position = SearchStatus.searchStatusPositionByTweetScreenname(editText.getText().toString(), // ユーザ名
                                            adapter.getList(), ((LinearLayoutManager) adapter.getRecyclerView().getLayoutManager()).findFirstVisibleItemPosition());
                                    adapter.scrollTo(position);
                                } else if (menuItemArray[which].equals(getString(R.string.action_tl_search_text))) {
                                    final int position = SearchStatus.searchStatusPositionByTweetText(editText.getText().toString(), // 本文
                                            adapter.getList(), ((LinearLayoutManager) adapter.getRecyclerView().getLayoutManager()).findFirstVisibleItemPosition());
                                    adapter.scrollTo(position);
                                } else if (menuItemArray[which].equals(getString(R.string.action_tl_search_createat))) {
                                    final Calendar cal = Cal.toCalendar(editText.getText().toString());
                                    if (cal != null) {
                                        final int position = BinarySearchUtil.binary_search_time(cal.getTime().getTime(), // 投稿日時
                                                adapter.getList(), ((LinearLayoutManager) adapter.getRecyclerView().getLayoutManager()).findFirstVisibleItemPosition());

                                        adapter.scrollTo(position);
                                    }
                                } else if (menuItemArray[which].equals(getString(R.string.action_tl_search_source))) {
                                    final int position = SearchStatus.searchStatusPositionByTweetSource(editText.getText().toString(), // クライアント名
                                            adapter.getList(), ((LinearLayoutManager) adapter.getRecyclerView().getLayoutManager()).findFirstVisibleItemPosition());
                                    adapter.scrollTo(position);
                                }
                            }
                        }
                    });
            return accountDlg.create();
        }
    }
}
