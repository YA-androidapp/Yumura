package jp.gr.java_conf.ya.yumura; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.EditText;

import java.util.Date;

import jp.gr.java_conf.ya.yumura.Layout.EndlessScrollListener;
import jp.gr.java_conf.ya.yumura.Layout.LinearLayoutManagerWithSmoothScroller;
import jp.gr.java_conf.ya.yumura.Network.CheckConnectivity;
import jp.gr.java_conf.ya.yumura.Setting.PreferenceActivity;
import jp.gr.java_conf.ya.yumura.Setting.PreferenceManage;
import jp.gr.java_conf.ya.yumura.String.IntentString;
import jp.gr.java_conf.ya.yumura.String.ViewString;
import jp.gr.java_conf.ya.yumura.Time.Time;
import jp.gr.java_conf.ya.yumura.Twitter.BinarySearchUtil;
import jp.gr.java_conf.ya.yumura.Twitter.KeyManage;
import jp.gr.java_conf.ya.yumura.Twitter.TwitterAccess;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;

public class TlActivity extends AppCompatActivity {
    public static OAuthAuthorization oAuthAuthorization;

    public static RequestToken requestToken;
    private static String preSearchtweetString = "";
    private Date preOnLoadMoreTime = new Date();
    private Date preSwipeRefreshTime = new Date();
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
        if (id == R.id.action_delJustBefore) {
            delJustBefore();
            return true;
        } else if (id == R.id.action_makeShortcut) {
            makeShortcut(searchViewString);
            return true;
        } else if (id == R.id.action_move_to_unread) {
            moveToUnread();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferenceActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setViews();

        if (CheckConnectivity.isConnected()) {
            if (KeyManage.getUserCount() < 1) {
                (new DialogFragment_ChangeOAuthKey()).show(getSupportFragmentManager(), getString(R.string.action_change_consumer_key));
            } else {
                pref_tl_api_count = PreferenceManage.getInt(this, "pref_tl_api_count", 200);
                final String url = IntentString.getUrlFromIntent(getIntent(), TwitterAccess.URL_PROTOCOL + TwitterAccess.URL_TWITTER);
                setSearchWord(url);
            }
        }
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
                final Status status = TwitterAccess.getStatusJustBefore(KeyManage.getCurrentUser().screenName);

                runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(TlActivity.this);
                        builder.setTitle(R.string.action_delJustBefore);
                        builder.setMessage(ViewString.getScreennameAndText(status));
                        builder.setPositiveButton(R.string.action_delJustBefore_del,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public final void run() {
                                                adapter = new TlAdapter(TlActivity.this, recyclerView);
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
                    } catch (Exception e) {
                        Log.v("Yumura", e.getMessage());
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.v("Yumura", e.getMessage());
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

    private final void moveToUnread() {
        long lastReadTweet = -1;
        lastReadTweet = PreferenceManage.getLong(this, PreferenceManage.Last_Read_Tweet, 0);
        Log.v("Yumura", "lastReadTweet: " + Long.toString(lastReadTweet));
        if (lastReadTweet > 0){

            Log.v("Yumura", "id[0]: " + adapter.getItemId(0));
            Log.v("Yumura", "id[adapter.getItemCount()-1]: " + adapter.getItemId(adapter.getItemCount() - 1));

            if ((adapter.getItemId(0) >= lastReadTweet) && (adapter.getItemId(adapter.getItemCount() - 1) <= lastReadTweet)) {
                int position = BinarySearchUtil.binary_search(lastReadTweet, adapter.getList());
                adapter.scrollTo(position);
                Log.v("Yumura", "Last_Read_Tweet getLong " + Long.toString(adapter.getItemId(position)) + " " + position);
            } else {
                adapter.scrollTo(adapter.getItemCount() - 1);
            }
        }
    }

    private final void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new EndlessScrollListener((LinearLayoutManager) recyclerView.getLayoutManager()) {
            @Override
            public void onLoadMore(int page) {
                swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
                if (!swipeRefresh.isRefreshing()) {
                    if (Time.differenceMinutes(preOnLoadMoreTime) > 0) {
                        changeRefreshLayoutIcon(true);

                        if (twitterAccess == null)
                            twitterAccess = new TwitterAccess(adapter);

                        try {
                            twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, adapter.getList().get(adapter.getItemCount() - 1).getId() - 1, -1, -1);
                        } catch (Exception e) {
                            twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, -1);
                        }

                        preOnLoadMoreTime = new Date();
                    }
                }
                changeRefreshLayoutIcon(false);
            }
        });
    }

    private final boolean setSearchWord(final String searchWord) {
//        Log.v("Yumura", "setSearchWord("+searchWord+")");
        try {
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(searchWord);
            actionBar.setDisplayShowTitleEnabled(true);
            if (searchWord != null && !searchWord.equals("")) {
                if (CheckConnectivity.isConnected()) {
                    if (searchViewString.equals(searchWord)) {
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
                        searchViewString = searchWord;
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
            Log.v("Yumura", e.getMessage());
        }

        return false;
    }

    private final void setViews() {
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
                if (!swipeRefresh.isRefreshing()) {
                    if (Time.differenceMinutes(preSwipeRefreshTime) > 0) {
                        if (twitterAccess == null)
                            twitterAccess = new TwitterAccess(adapter);
                        try {
                            twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, adapter.getList().get(0).getId() + 1);
                        } catch (Exception e) {
                            twitterAccess.loadTimeline(searchViewString, pref_tl_api_count, -1, -1, -1);
                        }

                        preSwipeRefreshTime = new Date();
                    }
                }
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
                                    adapter = new TlAdapter(getActivity(), null);
                                    final TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                    final StatusUpdate statusUpdate = new StatusUpdate(editText.getText().toString());
                                    twitterAccess.updateStatus(menuItemArray[which].replace("@", ""), statusUpdate);
                                } catch (Exception e) {
                                }
                            }
                        }
                    });
            return accountDlg.create();
        }
    }
}
