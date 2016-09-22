package jp.gr.java_conf.ya.yumura.Twitter; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.gr.java_conf.ya.yumura.App;
import jp.gr.java_conf.ya.yumura.Cache.LruCacheMap;
import jp.gr.java_conf.ya.yumura.Network.CommunicationVolume;
import jp.gr.java_conf.ya.yumura.Setting.PreferenceManage;
import jp.gr.java_conf.ya.yumura.String.ViewString;
import jp.gr.java_conf.ya.yumura.TlAdapter;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterListener;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAccess {
    public static final String CALLBACK_URL = "callback://CallbackActivity";
    public static final String CALLBACK_URL_VERIFIER = "oauth_verifier";
    public static final String CONSUMER_KEY = "jo2l8yatSpA6bxQFp3LuHA";
    public static final String CONSUMER_SECRET = "CvEQUYFW5b5esg0beWo2XuR1xjJnveNQrMhd9yf64";
    public static final String URL_PROTOCOL_HTTP = "http://";
    public static final String URL_PROTOCOL = "https://";
    public static final String URL_TWITTER = "twitter.com";
    public static final String URL_TWITTER_FAVORITE_1 = "i";
    public static final String URL_TWITTER_FAVORITE_2 = "likes";
    public static final String URL_TWITTER_MENTION = "mentions";
    public static final String URL_TWITTER_SEARCH = "twitter.com/search?q=";
    public static final String URL_TWITTER_SEARCH_REGEXP = "twitter[.]com[/]search[?]q=";
    private static final String URL_TWITTER_LIST = "lists";
    private static final String URL_TWITTER_STATUS = "status";
    private static boolean pref_debug_write_logcat = false;
    private static long[] preCommunicationVolume = {0L, 0L};
    private static Map<String, Long> cacheIdSn = new LruCacheMap<>(100);
    private static Map<Map<String, String>, Long> cacheIdSnSlug = new LruCacheMap<>(100);
    private static TlAdapter adapter;

    private static final TwitterListener mListener = new TwitterAdapter() {

        @Override
        public void createdFavorite(Status status) {
            if (adapter != null) {
                adapter.showSnackbar("Favorited", ViewString.getScreennameAndText(status));
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void destroyedFavorite(Status status) {
            if (adapter != null) {
                adapter.showSnackbar("Favorite Removed", ViewString.getScreennameAndText(status));
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void destroyedStatus(Status destroyedStatus) {
            if (adapter != null) {
                List<Status> destroyedStatuss = new ArrayList<>();
                destroyedStatuss.add(destroyedStatus);
                adapter.removeDataOf(destroyedStatuss);
                adapter.notifyDataSetChanged();
                adapter.showSnackbar("Removed", ViewString.getScreennameAndText(destroyedStatus));
            }
        }

        private void addDataOfAndNotify(final ResponseList<Status> statuses) {
            if (adapter != null) {
                ((Activity) adapter.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {
                        adapter.addDataOf(statuses);
                        adapter.notifyDataSetChanged();
                        adapter.moveToStartPositionOfReading();
                        adapter.showSnackbar("Loaded", Integer.toString(statuses.size()) + " tweets ; " + getCommunicationVolumeOfThisApp() + " B");
                    }
                });
            }
        }

        @Override
        public void gotHomeTimeline(final ResponseList<Status> statuses) {
            if (pref_debug_write_logcat) Log.i("Yumura", "gotHomeTimeline()");
            addDataOfAndNotify(statuses);
        }

        @Override
        public void gotMentions(final ResponseList<Status> statuses) {
            if (pref_debug_write_logcat) Log.i("Yumura", "gotMentions()");
            addDataOfAndNotify(statuses);
        }

        @Override
        public void gotUserListStatuses(final ResponseList<Status> statuses) {
            if (pref_debug_write_logcat) Log.i("Yumura", "gotUserListStatuses()");
            addDataOfAndNotify(statuses);
        }

        @Override
        public void gotUserTimeline(final ResponseList<Status> statuses) {
            if (pref_debug_write_logcat) Log.i("Yumura", "gotUserTimeline()");
            addDataOfAndNotify(statuses);
        }

        @Override
        public void retweetedStatus(final Status retweetedStatus) {
            if (pref_debug_write_logcat) Log.i("Yumura", "retweetedStatus()");
            ((Activity) adapter.getContext()).runOnUiThread(new Runnable() {
                @Override
                public final void run() {
                    if (adapter != null) {
                        adapter.showSnackbar("Retweeted", ViewString.getScreennameAndText(retweetedStatus));
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void searched(final QueryResult queryResult) {
            if (pref_debug_write_logcat) Log.i("Yumura", "searched()");
            ((Activity) adapter.getContext()).runOnUiThread(new Runnable() {
                @Override
                public final void run() {
                    if (adapter != null) {
                        adapter.addDataOf(queryResult.getTweets());
                        adapter.notifyDataSetChanged();
                        adapter.moveToStartPositionOfReading();
                        adapter.showSnackbar("Loaded", Integer.toString(queryResult.getTweets().size()) + " tweets ; " + getCommunicationVolumeOfThisApp() + " B");
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void updatedStatus(final Status status) {
            ((Activity) adapter.getContext()).runOnUiThread(new Runnable() {
                @Override
                public final void run() {
                    if (adapter != null) {
                        adapter.showSnackbar("Updated", ViewString.getScreennameAndText(status));
                        adapter.addDataOf(status);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    public TwitterAccess(final TlAdapter adapter) {
        this.adapter = adapter;

        pref_debug_write_logcat = PreferenceManage.getBoolean(App.getContext(), "pref_debug_write_logcat", false);

        getAsyncTwitter();
    }

    public static AsyncTwitter getAsyncTwitter() {
        return getAsyncTwitter("");
    }

    public static AsyncTwitter getAsyncTwitter(final String screenName) {
        try {
            OAuthUser currentUser = (screenName.equals("")) ? KeyManage.getCurrentUser() : KeyManage.getUser(KeyManage.getIdFromScreenNameAtPreference(screenName));

            if (currentUser == null)
                currentUser = KeyManage.getCurrentUser();

            if (currentUser != null) {
                final AccessToken accessToken = new AccessToken(currentUser.token, currentUser.tokenSecret);
                final AsyncTwitter asyncTwitter = new AsyncTwitterFactory(getConfiguration(currentUser.consumerKey, currentUser.consumerSecret)).getInstance(accessToken);
                asyncTwitter.addListener(mListener);
                return asyncTwitter;
            }
        } catch (Exception e) {
            if (pref_debug_write_logcat)
                Log.e("Yumura", "getAsyncTwitter(String) E: " + e.getMessage());
        }

        final AsyncTwitter asyncTwitter = new AsyncTwitterFactory(getConfiguration(CONSUMER_KEY, CONSUMER_SECRET)).getInstance();
        asyncTwitter.addListener(mListener);
        return asyncTwitter;
    }

    public static String[] getColors() {
        try {
            String screenName = KeyManage.getCurrentUser().screenName;
            final User user = getTwitter(screenName).showUser(screenName);
            return new String[]{"#" + user.getProfileBackgroundColor(),
                    "#" + user.getProfileTextColor()};
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", "getColors() E: " + e.getMessage());
        }

        return new String[]{"#ffffff", "#000000"};
    }

    public static Configuration getConfiguration(final String consumerKey, final String consumerSecret) {
        final ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(consumerKey);
        cb.setOAuthConsumerSecret(consumerSecret);
        cb.setHttpConnectionTimeout(30 * 1000);   // HTTP接続タイムアウト 20000
        cb.setHttpReadTimeout(30 * 1000);         // HTTPリードタイムアウト 120000
        cb.setHttpRetryCount(2);                // HTTPリトライ回数	0
        cb.setHttpRetryIntervalSeconds(30);     // HTTPリトライ間隔 5

        return cb.build();
    }

    public static Status getStatusJustBefore(final String screenName) {
        if (!screenName.equals("")) {
            try {
                final User user = getTwitter(screenName).showUser(screenName);
                Status status = user.getStatus();
                status = getTwitter(screenName).showStatus(status.getId());
                if (status != null)
                    return status;
            } catch (Exception e) {
                if (pref_debug_write_logcat)
                    Log.e("Yumura", "getStatusJustBefore() E: " + e.getMessage());
            }
        }

        return null;
    }

    public static ResponseList<Status> getStatusesJustBefore(final String screenName) {
        final Paging paging = new Paging();
        paging.setCount(10);
        try {
            return getTwitter(screenName).getUserTimeline(screenName, paging);
        } catch (Exception e) {
            return null;
        }
    }

    public static Twitter getTwitter() {
        return getTwitter("");
    }

    public static Twitter getTwitter(final String screenName) {
        try {
            OAuthUser currentUser = (screenName.equals("")) ? KeyManage.getCurrentUser() : KeyManage.getUser(KeyManage.getIdFromScreenNameAtPreference(screenName));

            if (currentUser == null)
                currentUser = KeyManage.getCurrentUser();

            if (currentUser != null) {
                final AccessToken accessToken = new AccessToken(currentUser.token, currentUser.tokenSecret);
                final Twitter twitter = new TwitterFactory(getConfiguration(currentUser.consumerKey, currentUser.consumerSecret)).getInstance(accessToken);
                return twitter;
            }
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", "getTwitter(String) E: " + e.getMessage());
        }

        return (new TwitterFactory(getConfiguration(CONSUMER_KEY, CONSUMER_SECRET))).getSingleton();
    }

    public static Status showStatus(final long id) {
        if (id > -1) {
            try {
                return getTwitter().showStatus(id);
            } catch (Exception e) {
                if (pref_debug_write_logcat) Log.e("Yumura", "showStatus() E: " + e.getMessage());
            }
        }

        return null;
    }

    public static String[] getUserListUrls(String screenName) {
        if ((screenName == null) || (screenName.equals(""))) {
            screenName = KeyManage.getCurrentUser().screenName;
        }

        List<String> urls = new ArrayList<String>();

        final Twitter twitter = getTwitter(screenName);

        ResponseList<UserList> lists = null;
        try {
            lists = twitter.getUserLists(screenName);
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", "getUserListUrls() E: " + e.getMessage());
        }
        if (lists.size() > 0) {
            for (UserList list : lists) {
                if (KeyManage.isAuthenticatedUser(list.getUser().getScreenName())) {
                    urls.add(list.getURI().toString());
                }
            }
        }

        return urls.toArray(new String[0]);
    }

    private static String getCommunicationVolumeOfThisApp() {
        try {
            if (pref_debug_write_logcat)
                Log.i("Yumura", "String getCommunicationVolumeArrayOfThisApp()");
            final long[] currentCommunicationVolume = CommunicationVolume.getCommunicationVolumeArrayOfThisApp();
            if ((currentCommunicationVolume[0] > -1) && (currentCommunicationVolume[1] > -1)) {
                final long uidTxBytes = currentCommunicationVolume[0] - preCommunicationVolume[0];
                final long uidRxBytes = currentCommunicationVolume[1] - preCommunicationVolume[1];
                preCommunicationVolume = currentCommunicationVolume;

                if (pref_debug_write_logcat) {
                    Log.i("Yumura", "getCommunicationVolumeArrayOfThisApp() uidRxBytes:" + String.format(Locale.JAPAN, "%,d", uidRxBytes));
                    Log.i("Yumura", "getCommunicationVolumeArrayOfThisApp() uidTxBytes:" + String.format(Locale.JAPAN, "%,d", uidTxBytes));
                }

                final long uidBytes = uidTxBytes + uidRxBytes;

                final String uidByteString = String.format(Locale.JAPAN, "%,d", uidBytes);
                if (pref_debug_write_logcat)
                    Log.i("Yumura", "getCommunicationVolumeArrayOfThisApp() " + uidByteString);
                return uidByteString;
            }
        } catch (Exception e) {
        }

        return "";
    }

    public void createFavorite(final String screenName, final Status status) {
        if (status != null) {
            try {
                getAsyncTwitter(screenName).createFavorite(status.getId());
            } catch (Exception e) {
                if (pref_debug_write_logcat)
                    Log.e("Yumura", "createFavorite() E: " + e.getMessage());
            }
        }
    }

    public void destroyFavorite(final String screenName, final Status status) {
        if (status != null) {
            try {
                getAsyncTwitter(screenName).destroyFavorite(status.getId());
            } catch (Exception e) {
                if (pref_debug_write_logcat)
                    Log.e("Yumura", "destroyFavorite() E: " + e.getMessage());
            }
        }
    }

    public void destroyStatus(final String screenName, final Status status) {
        if (status != null) {
            try {
                getAsyncTwitter(screenName).destroyStatus(status.getId());
            } catch (Exception e) {
                if (pref_debug_write_logcat)
                    Log.e("Yumura", "destroyStatus() E: " + e.getMessage());
            }
        }
    }

    private void getFavorites(final int count, final long maxId, final int page, final long sinceId, final String authUser) {
        final Paging paging = new Paging();
        if (count > -1)
            paging.setCount(count);
        if (maxId > -1)
            paging.setMaxId(maxId);
        if (page > -1)
            paging.setPage(page);
        if (sinceId > -1)
            paging.setSinceId(sinceId);
        getAsyncTwitter(authUser).getFavorites(paging);
        if (pref_debug_write_logcat) Log.i("Yumura", "getFavorites(" + paging.toString() + ")");
    }

    private void getHomeTimeline(final int count, final long maxId, final int page, final long sinceId, final String authUser) {
        final Paging paging = new Paging();
        if (count > -1)
            paging.setCount(count);
        if (maxId > -1)
            paging.setMaxId(maxId);
        if (page > -1)
            paging.setPage(page);
        if (sinceId > -1)
            paging.setSinceId(sinceId);
        getAsyncTwitter(authUser).getHomeTimeline(paging);
        if (pref_debug_write_logcat) Log.i("Yumura", "getHomeTimeline(" + paging.toString() + ")");
    }

    private long getIdFromScreenName(final String screenName) {
        long cachedId = -1L;
        try {
            cachedId = cacheIdSn.get(screenName);
        } catch (Exception e) {
            if (pref_debug_write_logcat)
                Log.e("Yumura", "getIdFromScreenName() E: " + e.getMessage());
        }
        if (cachedId > -1) {
            // キャッシュにある場合
            return cachedId;
        } else {
            // キャッシュにない場合
            final long userId = KeyManage.getIdFromScreenNameAtPreference(screenName);
            if (userId < 0) {
                try {
                    cacheIdSn.put(screenName, getTwitter().showUser(screenName).getId());
                } catch (Exception e) {
                }
                try {
                    return getTwitter().showUser(screenName).getId();
                } catch (Exception e) {
                }
            }
        }
        return -1L;
    }

    private long getListIdFromSlug(String screenName, final String slug) {
        if ((screenName == null) || (screenName.equals(""))) {
            screenName = KeyManage.getCurrentUser().screenName;
        }

        final Map<String, String> keys = new HashMap<>();
        keys.put(screenName, slug);

        long cachedListId = -1L;
        try {
            cachedListId = cacheIdSnSlug.get(keys);
        } catch (Exception e1) {
            if (pref_debug_write_logcat)
                Log.e("Yumura", "getListIdFromSlug() E1: " + e1.getMessage());
        }
        if (cachedListId > -1) {
            // キャッシュにある場合
            return cachedListId;
        } else {
            // キャッシュにない場合
            final Twitter twitter = getTwitter(screenName);

            ResponseList<UserList> lists = null;
            try {
                lists = twitter.getUserLists(screenName);
            } catch (Exception e2) {
                if (pref_debug_write_logcat)
                    Log.e("Yumura", "getListIdFlomSlug() E2: " + e2.getMessage());
            }
            if ((lists != null) && (lists.size() > 0)) {
                for (UserList list : lists) {
                    if (list != null) {
                        if ((list.getSlug().equals(slug))
                                || (list.getName().toLowerCase().equals(slug.toLowerCase()))) {
                            try {
                                cacheIdSnSlug.put(keys, list.getId());
                            } catch (Exception e3) {
                                if (pref_debug_write_logcat)
                                    Log.e("Yumura", "getListIdFromSlug() E3: " + e3.getMessage());
                            }
                            try {
                                return list.getId();
                            } catch (Exception e4) {
                                if (pref_debug_write_logcat)
                                    Log.e("Yumura", "getListIdFromSlug() E4: " + e4.getMessage());
                            }
                        }
                    }
                }
            }
        }

        return -1L;
    }

    private void getMentions(final int count, final long maxId, final int page, final long sinceId, final String authUser) {
        final Paging paging = new Paging();
        if (count > -1)
            paging.setCount(count);
        if (maxId > -1)
            paging.setMaxId(maxId);
        if (page > -1)
            paging.setPage(page);
        if (sinceId > -1)
            paging.setSinceId(sinceId);
        getAsyncTwitter(authUser).getMentions(paging);
        if (pref_debug_write_logcat) Log.i("Yumura", "getMentions(" + paging.toString() + ")");
    }

    private void getUserTimeline(final String screenName, final int count, final long maxId, final int page, final long sinceId) {
        final Paging paging = new Paging();
        if (count > -1)
            paging.setCount(count);
        if (maxId > -1)
            paging.setMaxId(maxId);
        if (page > -1)
            paging.setPage(page);
        if (sinceId > -1)
            paging.setSinceId(sinceId);
        getAsyncTwitter().getUserTimeline(screenName, paging);
        if (pref_debug_write_logcat) Log.i("Yumura", "getUserTimeline(" + paging.toString() + ")");
    }

    private void getUserListStatuses(final String screenName, final String slug, final int count, final long maxId, final int page, final long sinceId) {
        new Thread(new Runnable() {
            @Override
            public final void run() {
                // final long userId = getIdFromScreenName(screenName);
                final long listId = getListIdFromSlug(screenName, slug);
                if (listId > -1) {
                    final Paging paging = new Paging();
                    if (count > -1)
                        paging.setCount(count);
                    if (maxId > -1)
                        paging.setMaxId(maxId);
                    if (page > -1)
                        paging.setPage(page);
                    if (sinceId > -1)
                        paging.setSinceId(sinceId);
                    getAsyncTwitter().getUserListStatuses(listId, paging);
                }
            }
        }).start();
    }

    public String[] loadTimeline(final String url, final int count, final long maxId, final int page, final long sinceId) {

        preCommunicationVolume = CommunicationVolume.getCommunicationVolumeArrayOfThisApp();

        List<String> result = new ArrayList<>();

        // " https://twitter.comhttps://twitter.com/#auth https://twitter.com/foobarhttps://twitter.com/foobar/lists/hogehttps://twitter.com/search?q=#ntvhttps://twitter.com/mentions "
        // "", "twitter.com", "twitter.com/#auth ", "twitter.com/foobar", "twitter.com/foobar/lists/hoge", "twitter.com/search?q=#ntv", "twitter.com/mentions"
        final String[] urls = url.trim().replaceAll(URL_PROTOCOL_HTTP, URL_PROTOCOL).split(URL_PROTOCOL);

        for (final String ur : urls) {
            String u = ur.trim();
            if (!u.equals("") && (u.startsWith(URL_TWITTER))) {
                if (pref_debug_write_logcat) Log.i("Yumura", "loadTimeline(" + url + ") u:" + u);
                // "twitter.com", "twitter.com/#auth", "twitter.com/foobar", "twitter.com/foobar/lists/hoge", "twitter.com/search?q=#ntv", "twitter.com/mentions"


                if (u.startsWith(URL_TWITTER_SEARCH)) {
                    if (pref_debug_write_logcat)
                        Log.i("Yumura", "loadTimeline(" + url + ") 検索 u:" + u);
                    // "twitter.com/search?q=#ntv"
                    final String[] uSplitBySEARCH = u.split(URL_TWITTER_SEARCH_REGEXP);
                    if (pref_debug_write_logcat)
                        Log.i("Yumura", "loadTimeline(" + url + ") 検索 u[1]:" + uSplitBySEARCH[uSplitBySEARCH.length - 1]);
                    final String queryStr = uSplitBySEARCH[uSplitBySEARCH.length - 1];
                    if (pref_debug_write_logcat)
                        Log.i("Yumura", "loadTimeline(" + url + ") 検索 queryStr:" + queryStr);
                    if (!queryStr.equals(""))
                        search(count, maxId, queryStr, sinceId);

                } else {
                    if (pref_debug_write_logcat)
                        Log.i("Yumura", "loadTimeline(" + url + ") 検索以外 u:" + u);
                    // "twitter.com", "twitter.com/#auth", "twitter.com/foobar", "twitter.com/foobar/lists/hoge", "twitter.com/mentions"
                    String authUser = "";

                    if (u.contains("#")) {
                        if (pref_debug_write_logcat)
                            Log.i("Yumura", "loadTimeline(" + url + ") 認証ユーザ u:" + u);
                        final String[] uSplitBySharp = u.split("#");
                        authUser = uSplitBySharp[uSplitBySharp.length - 1];
                        u = u.substring(0, u.lastIndexOf("#"));
                    }

                    // "twitter.com", "twitter.com/", "twitter.com/foobar", "twitter.com/foobar/lists/hoge",  "twitter.com/mentions"
                    final String[] uSplitBySlash = u.split("/");

                    if ((uSplitBySlash.length == 1) ||
                            ((uSplitBySlash.length == 2) && (uSplitBySlash[1].equals("")))
                            ) {
                        if (pref_debug_write_logcat)
                            Log.i("Yumura", "loadTimeline(" + url + ") ホーム u:" + u);
                        // "twitter.com/", "twitter.com/"
                        getHomeTimeline(count, maxId, page, sinceId, authUser);
                    } else if ((uSplitBySlash.length > 1) && (uSplitBySlash.length < 4)) {
                        if (uSplitBySlash[1].equals(URL_TWITTER_MENTION)) {
                            if (pref_debug_write_logcat)
                                Log.i("Yumura", "loadTimeline(" + url + ") リプライ u:" + u);
                            // "twitter.com/mentions"
                            getMentions(count, maxId, page, sinceId, authUser);
                        } else {
                            if (pref_debug_write_logcat)
                                Log.i("Yumura", "loadTimeline(" + url + ") ユーザ u:" + u);
                            // "twitter.com/foobar"
                            final String screenName = uSplitBySlash[1];
                            getUserTimeline(screenName, count, maxId, page, sinceId);
                        }
                        break;
                    } else if ((uSplitBySlash.length > 2)
                            && (uSplitBySlash.length < 5)
                            && (uSplitBySlash[1].equals(URL_TWITTER_FAVORITE_1))
                            && (uSplitBySlash[2].equals(URL_TWITTER_FAVORITE_2))) {
                        if (pref_debug_write_logcat)
                            Log.i("Yumura", "loadTimeline(" + url + ") ふぁぼ u:" + u);
                        // "twitter.com/i/likes"
                        getFavorites(count, maxId, page, sinceId, authUser);
                        break;
                    } else if ((uSplitBySlash.length >= 4) && (uSplitBySlash[2].equals(URL_TWITTER_LIST))) {
                        if (pref_debug_write_logcat)
                            Log.i("Yumura", "loadTimeline(" + url + ") リスト u:" + u);
                        // "twitter.com/foobar/lists/hoge"
                        final String screenName = uSplitBySlash[1];
                        final String slug = uSplitBySlash[3];
                        getUserListStatuses(screenName, slug, count, maxId, page, sinceId);
                    } else if ((uSplitBySlash.length >= 4) && (uSplitBySlash[2].equals(URL_TWITTER_STATUS))) {
                        if (pref_debug_write_logcat)
                            Log.i("Yumura", "loadTimeline(" + url + ") ステータス u:" + u);
                        // "twitter.com/foobar/status/hoge"
                        final String screenName = uSplitBySlash[1];
                        final String id = uSplitBySlash[3];
                        getUserTimeline(screenName, 5, Long.parseLong(id), page, sinceId);
                        getUserTimeline(screenName, 5, maxId, page, Long.parseLong(id));
                    }
                }
                result.add(u);
            } else if (!u.equals("")) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_PROTOCOL + u));
                i.setClassName("jp.gr.java_conf.ya.yumura",
                        "jp.gr.java_conf.ya.yumura.ViewerActivity");
                try {
                    if (adapter != null)
                        if (adapter.getContext() != null)
                            ((Activity) adapter.getContext()).startActivityForResult(i, 0);
                } catch (Exception e) {
                    if (pref_debug_write_logcat)
                        Log.e("Yumura", "loadTimeline() E: " + e.getMessage());
                }
            }
        }

        return result.toArray(new String[0]);
    }

    public void pak(final String screenName, final Status status) {
        if (status != null) {
            try {
                getAsyncTwitter(screenName).updateStatus(status.getText());
            } catch (Exception e) {
                if (pref_debug_write_logcat) Log.e("Yumura", "pak() E: " + e.getMessage());
            }
        }
    }

    public void retweet(final String screenName, final Status status) {
        if (status != null) {
            try {
                getAsyncTwitter(screenName).retweetStatus(status.getId());
            } catch (Exception e) {
                if (pref_debug_write_logcat) Log.e("Yumura", "retweet() E: " + e.getMessage());
            }
        }
    }

    private void search(final int count, final long maxId, String text, final long sinceId) {
        if (!text.equals("")) {
            final Query query = new Query();
            if (count > -1)
                query.setCount(count);
            if (maxId > -1)
                query.setMaxId(maxId);
            if (sinceId > -1)
                query.setSinceId(sinceId);

            if (pref_debug_write_logcat) Log.i("Yumura", "search() setQuery: " + text);
            query.setQuery(text);
            query.setResultType(Query.RECENT);
            getAsyncTwitter().search(query);
        }
    }

    public void updateStatus(final String screenName, final String statusText) {
        if ((!screenName.equals("")) && (!statusText.equals(""))) {
            try {
                getAsyncTwitter(screenName).updateStatus(statusText);
            } catch (Exception e) {
                if (pref_debug_write_logcat)
                    Log.e("Yumura", "updateStatus(String, String) E: " + e.getMessage());
            }
        }
    }

    public void updateStatus(final String screenName, final StatusUpdate statusUpdate) {
        if ((!screenName.equals("")) && (statusUpdate != null)) {
            try {
                getAsyncTwitter(screenName).updateStatus(statusUpdate);
            } catch (Exception e) {
                if (pref_debug_write_logcat)
                    Log.e("Yumura", "updateStatus(String, StatusUpdate) E: " + e.getMessage());
            }
        }
    }

    public void userRetweet(final String screenName, final Status status) {
        if (status != null) {
            try {
                getAsyncTwitter(screenName).updateStatus("RT " + ViewString.getScreennameAndText(status));
            } catch (Exception e) {
                if (pref_debug_write_logcat) Log.e("Yumura", "userRetweet() E: " + e.getMessage());
            }
        }
    }

}
