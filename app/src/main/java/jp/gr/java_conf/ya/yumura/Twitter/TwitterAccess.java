package jp.gr.java_conf.ya.yumura.Twitter; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.ya.yumura.Cache.LruCacheMap;
import jp.gr.java_conf.ya.yumura.String.ViewString;
import jp.gr.java_conf.ya.yumura.TlActivity;
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
import twitter4j.UserList;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAccess {

    public static final String CALLBACK_URL = "callback://CallbackActivity";
    public static final String CALLBACK_URL_VERIFIER = "oauth_verifier";
    public static final String CONSUMER_KEY = "jo2l8yatSpA6bxQFp3LuHA";
    public static final String CONSUMER_SECRET = "CvEQUYFW5b5esg0beWo2XuR1xjJnveNQrMhd9yf64";
    public static final String URL_PROTOCOL = "https://";
    public static final String URL_TWITTER = "twitter.com";
    private static final String URL_TWITTER_LIST = "lists";
    private static final String URL_TWITTER_MENTION = "mentions";
    private static final String URL_TWITTER_SEARCH = "twitter.com/search?q=";
    private static Map<String, Long> cacheIdSn = new LruCacheMap<>(100);
    private static Map<Map<String, String>, Long> cacheIdSnSlug = new LruCacheMap<>(100);
    private static TlAdapter adapter;
    private static final TwitterListener mListener = new TwitterAdapter() {
        @Override
        public void gotHomeTimeline(final ResponseList<Status> statuses) {
            if (adapter != null) {
                adapter.addDataOf(statuses);
                adapter.notifyDataSetChanged();
                adapter.notify();
            }
        }

        @Override
        public void gotMentions(final ResponseList<Status> statuses) {
            if (adapter != null) {
                adapter.addDataOf(statuses);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void gotUserListStatuses(final ResponseList<Status> statuses) {
            if (adapter != null) {
                adapter.addDataOf(statuses);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void gotUserTimeline(final ResponseList<Status> statuses) {
            if (adapter != null) {
                adapter.addDataOf(statuses);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void searched(final QueryResult queryResult) {
            if (adapter != null) {
                adapter.addDataOf(queryResult.getTweets());
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void updatedStatus(final Status status) {
            TlActivity.showSnackbar(ViewString.getScreennameAndText(status), "updatedStatus");
        }

    };

    public TwitterAccess(final TlAdapter adapter) {
        this.adapter = adapter;

        getAsyncTwitter();
    }

    public static AsyncTwitter getAsyncTwitter() {
        return getAsyncTwitter("");
    }

    public static AsyncTwitter getAsyncTwitter(final String screenName) {
        OAuthUser currentUser = (screenName.equals("")) ? KeyManage.getCurrentUser() : KeyManage.getUser(KeyManage.getIdFromScreenNameAtPreference(screenName));

        if (currentUser == null)
            currentUser = KeyManage.getCurrentUser();

        if (currentUser != null) {
            final AccessToken accessToken = new AccessToken(currentUser.token, currentUser.tokenSecret);
            final AsyncTwitter asyncTwitter = new AsyncTwitterFactory(getConfiguration()).getInstance();
            asyncTwitter.addListener(mListener);
            asyncTwitter.setOAuthConsumer(currentUser.consumerKey, currentUser.consumerSecret);
            asyncTwitter.setOAuthAccessToken(accessToken);
            return asyncTwitter;
        } else {
            final AsyncTwitter asyncTwitter = new AsyncTwitterFactory(getConfiguration()).getInstance();
            asyncTwitter.addListener(mListener);
            return asyncTwitter;
        }
    }

    public static Twitter getTwitter() {
        return getTwitter("");
    }

    public static Twitter getTwitter(final String screenName) {
        OAuthUser currentUser = (screenName.equals("")) ? KeyManage.getCurrentUser() : KeyManage.getUser(KeyManage.getIdFromScreenNameAtPreference(screenName));

        if (currentUser == null)
            currentUser = KeyManage.getCurrentUser();

        if (currentUser != null) {
            final AccessToken accessToken = new AccessToken(currentUser.token, currentUser.tokenSecret);
            final Twitter twitter = TwitterFactory.getSingleton();
            twitter.setOAuthConsumer(currentUser.consumerKey, currentUser.consumerSecret);
            twitter.setOAuthAccessToken(accessToken);
            return twitter;
        } else {
            return TwitterFactory.getSingleton();
        }
    }

    public static Configuration getConfiguration() {
        final ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setHttpConnectionTimeout(30 * 1000);   // HTTP接続タイムアウト 20000
        cb.setHttpReadTimeout(30 * 1000);         // HTTPリードタイムアウト 120000
        cb.setHttpRetryCount(2);                // HTTPリトライ回数	0
        cb.setHttpRetryIntervalSeconds(30);     // HTTPリトライ間隔 5

        return cb.build();
    }

    public String[] loadTimeline(final String url, final int count, final long maxId, final int page, final long sinceId) {
        List<String> result = new ArrayList<>();

        // " https://twitter.comhttps://twitter.com/#auth https://twitter.com/foobarhttps://twitter.com/foobar/lists/hogehttps://twitter.com/search?q=#ntvhttps://twitter.com/mentions "
        // "", "twitter.com", "twitter.com/#auth ", "twitter.com/foobar", "twitter.com/foobar/lists/hoge", "twitter.com/search?q=#ntv", "twitter.com/mentions"
        final String[] urls = url.trim().replaceAll("http://", "https://").split(URL_PROTOCOL);

        for (final String ur : urls) {
            String u = ur.trim();
            if (!u.equals("") && (u.startsWith(URL_TWITTER))) {
//                Log.v("Yumura", "loadTimeline(" + url + ") u:" + u);
                // "twitter.com", "twitter.com/#auth", "twitter.com/foobar", "twitter.com/foobar/lists/hoge", "twitter.com/search?q=#ntv", "twitter.com/mentions"


                if (u.startsWith(URL_TWITTER_SEARCH)) {
//                    Log.v("Yumura", "loadTimeline(" + url + ") 検索 u:" + u);
                    // "twitter.com/search?q=#ntv"
                    final String[] uSplitBySEARCH = u.split(URL_TWITTER_SEARCH);
                    final String queryStr = uSplitBySEARCH[uSplitBySEARCH.length - 1];
                    if (!queryStr.equals(""))
                        search(count, maxId, queryStr, sinceId);

                } else {
//                    Log.v("Yumura", "loadTimeline(" + url + ") 検索以外 u:" + u);
                    // "twitter.com", "twitter.com/#auth", "twitter.com/foobar", "twitter.com/foobar/lists/hoge", "twitter.com/mentions"
                    String authUser = "";

                    if (u.contains("#")) {
//                        Log.v("Yumura", "loadTimeline(" + url + ") 認証ユーザ u:" + u);
                        final String[] uSplitBySharp = u.split("#");
                        authUser = uSplitBySharp[uSplitBySharp.length - 1];
                        u = u.substring(0, u.lastIndexOf("#"));
                    }

                    // "twitter.com", "twitter.com/", "twitter.com/foobar", "twitter.com/foobar/lists/hoge",  "twitter.com/mentions"
                    final String[] uSplitBySlash = u.split("/");

                    if ((uSplitBySlash.length == 1) ||
                            ((uSplitBySlash.length == 2) && (uSplitBySlash[1].equals("")))
                            ) {
//                        Log.v("Yumura", "loadTimeline(" + url + ") ホーム u:" + u);
                        // "twitter.com/", "twitter.com/"
                        getHomeTimeline(count, maxId, page, sinceId, authUser);
                    } else if ((uSplitBySlash.length > 1) && (uSplitBySlash.length < 4)) {
                        if (uSplitBySlash[1].equals(URL_TWITTER_MENTION)) {
//                            Log.v("Yumura", "loadTimeline(" + url + ") リプライ u:" + u);
                            // "twitter.com/mentions"
                            getMentions(count, maxId, page, sinceId, authUser);
                        } else {
//                            Log.v("Yumura", "loadTimeline(" + url + ") ユーザ u:" + u);
                            // "twitter.com/foobar"
                            final String screenName = uSplitBySlash[1];
                            getUserTimeline(screenName, count, maxId, page, sinceId);
                        }
                        break;
                        //TODO https://twitter.com/i/likes
                    } else if ((uSplitBySlash.length >= 4) && (uSplitBySlash[2].equals(URL_TWITTER_LIST))) {
//                        Log.v("Yumura", "loadTimeline(" + url + ") リスト u:" + u);
                        // "twitter.com/foobar/lists/hoge"
                        final String screenName = uSplitBySlash[1];
                        final String slug = uSplitBySlash[3];
                        getUserListStatuses(screenName, slug, count, maxId, page, sinceId);
                    }
                }
                result.add(u);
            }
        }

        return result.toArray(new String[0]);
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
//        Log.v("Yumura", "getHomeTimeline(" + paging.toString() + ")");
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
//        Log.v("Yumura", "getMentions(" + paging.toString() + ")");
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
//        Log.v("Yumura", "getUserTimeline(" + paging.toString() + ")");
    }

    private long getIdFromScreenName(final String screenName) {
        long cachedId = -1L;
        try {
            cachedId = cacheIdSn.get(screenName);
        } catch (Exception e) {
        }
        if (cachedId > -1) {
            // キャッシュにある場合
            return cachedId;
        } else {
            // キャッシュにない場合
            long userId = KeyManage.getIdFromScreenNameAtPreference(screenName);
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
//        Log.v("Yumura", "getListIdFromSlug(" + screenName + ", " + slug + ")");

        if ((screenName == null) || (screenName.equals(""))) {
            screenName = KeyManage.getCurrentUser().screenName;
        }

        final Map<String, String> keys = new HashMap<>();
        keys.put(screenName, slug);

        long cachedListId = -1L;
        try {
            cachedListId = cacheIdSnSlug.get(keys);
        } catch (Exception e) {
        }
        if (cachedListId > -1) {
            // キャッシュにある場合
            return cachedListId;
        } else {
            // キャッシュにない場合
            final Twitter twitter = getTwitter(screenName);
//            Log.v("Yumura", "twitter: " + twitter.toString());

            ResponseList<UserList> lists = null;
            try {
                lists = twitter.getUserLists(screenName);
//                Log.v("Yumura", "getUserLists");
            } catch (Exception e) {
//                Log.v("Yumura", e.getMessage() + e.toString());
            }
//            Log.v("Yumura", "getListIdFromSlug() size: " + lists.toString());
            if (lists.size() > 0) {
//                Log.v("Yumura", "getListIdFromSlug() size: " + lists.size());
                for (UserList list : lists) {
//                    Log.v("Yumura", "getListIdFromSlug() " + list.getId() + ": " + list.getName() + ": " + list.getFullName());
                    if (list.getName().equals(slug)) {
                        try {
                            cacheIdSnSlug.put(keys, list.getId());
                        } catch (Exception e) {
                        }
                        try {
                            return list.getId();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }

        return -1L;
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
                    // getAsyncTwitter().getUserListStatuses(userId, slug, paging);
                    getAsyncTwitter().getUserListStatuses(listId, paging);
//                    Log.v("Yumura", "getUserTimeline(" + paging.toString() + ")");
                }
            }
        }).start();
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

            query.setQuery(text);
            query.setResultType(Query.RECENT);
            getAsyncTwitter().search(query);
        }
    }

    public void updateStatus(final String updateText) {
        if (!updateText.equals("")) {
            try {
                getAsyncTwitter().updateStatus(updateText);
            } catch (Exception e) {
            }
        }
    }

    public void updateStatus(final StatusUpdate statusUpdate) {
        if (statusUpdate != null) {
            try {
                getAsyncTwitter().updateStatus(statusUpdate);
            } catch (Exception e) {
            }
        }
    }

    public void retweet(final Status status) {
        if (status != null) {
            try {
                getAsyncTwitter().retweetStatus(status.getId());
            } catch (Exception e) {
            }
        }
    }

    public void userRetweet(final Status status) {
        if (status != null) {
            try {
                getAsyncTwitter().updateStatus("RT "+ViewString.getScreennameAndText(status));
            } catch (Exception e) {
            }
        }
    }

    public void favorite(final Status status) {
        if (status != null) {
            try {
                getAsyncTwitter().createFavorite(status.getId());
            } catch (Exception e) {
            }
        }
    }

    public void pak(final Status status) {
        if (status != null) {
            try {
                getAsyncTwitter().updateStatus(status.getText());
            } catch (Exception e) {
            }
        }
    }

}
