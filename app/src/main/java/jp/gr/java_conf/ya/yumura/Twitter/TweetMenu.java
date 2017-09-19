package jp.gr.java_conf.ya.yumura.Twitter; // Copyright (c) 2013-2017 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.ya.yumura.R;
import jp.gr.java_conf.ya.yumura.String.ViewString;
import jp.gr.java_conf.ya.yumura.Time.Time;
import jp.gr.java_conf.ya.yumura.TlAdapter;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.URLEntity;

public class TweetMenu {
    private static boolean pref_debug_write_logcat = false;
    private static Context context;
    private static TlAdapter adapter;

    public static void showTweetMenu(final Context c, final TlAdapter a, Status status) {
        context = c;
        adapter = a;

        if (status == null)
            return;

        if (Time.differenceMinutes(status.getCreatedAt()) > 0) {
            try {
                final Status tempStatus = TwitterAccess.showStatus(status.getId());
                if (tempStatus != null)
                    status = tempStatus;
            } catch (Exception e) {
                if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
            }
        }

        final Status statusMe = status;
        final Status statusRT = (status.getRetweetedStatus() != null) ? status.getRetweetedStatus() : status;

        final String screennameAndText = ViewString.getScreennameAndText(status);

        final List<String> menuItem = new ArrayList<>();
        menuItem.add("@" + statusMe.getUser().getScreenName());
        if (status.getRetweetedStatus() != null)
            menuItem.add("@" + statusRT.getUser().getScreenName());
        menuItem.add(context.getString(R.string.tweet_reply));
        menuItem.add(context.getString(R.string.tweet_retweet));
        menuItem.add(context.getString(R.string.tweet_userretweet));
        menuItem.add(context.getString(R.string.tweet_create_favorite));
        menuItem.add(context.getString(R.string.tweet_favoriteretweet));
        menuItem.add(context.getString(R.string.tweet_favoriteuserretweet));
        menuItem.add(context.getString(R.string.tweet_destroy_favorite));
        menuItem.add(context.getString(R.string.tweet_share_url));
        menuItem.add(context.getString(R.string.tweet_share_url_tweet));
        menuItem.add(context.getString(R.string.tweet_share_text));
        menuItem.add(context.getString(R.string.tweet_delete));
        menuItem.add(context.getString(R.string.tweet_pak));

        final String[] menuItemArray = menuItem.toArray(new String[0]);
        final AlertDialog.Builder listDlg = new AlertDialog.Builder(context);
        listDlg.setTitle(screennameAndText);
        listDlg.setItems(
                menuItemArray,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if (menuItemArray[which].startsWith("@")) {
                            tweetProfile(menuItemArray[which].substring(1));
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_reply))) {
                            tweetReply(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_retweet))) {
                            tweetRetweet(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_userretweet))) {
                            tweetUserRetweet(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_create_favorite))) {
                            tweetCreateFavorite(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_favoriteretweet))) {
                            tweetCreateFavorite(statusRT);
                            tweetRetweet(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_favoriteuserretweet))) {
                            tweetCreateFavorite(statusRT);
                            tweetUserRetweet(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_destroy_favorite))) {
                            tweetDestroyFavorite(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_share_url))) {
                            tweetShareUrl(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_share_url_tweet))) {
                            tweetShareUrlTweet(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_share_text))) {
                            tweetShareText(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_delete))) {
                            tweetDelete(statusMe);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_pak))) {
                            tweetPak(statusRT);
                        }
                    }
                });
        listDlg.create().show();

    }

    private static void tweetProfile(final String screenName) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("jp.gr.java_conf.ya.yumura", "jp.gr.java_conf.ya.yumura.TlActivity");
            intent.setData(Uri.parse(TwitterAccess.URL_PROTOCOL + TwitterAccess.URL_TWITTER + "/" + screenName));
            context.startActivity(intent);
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }
    }

    private static void tweetReply(final Status status) {
        final EditText editText = new EditText(context);
        editText.setHint(R.string.action_update_text);
        editText.setText("@" + status.getUser().getScreenName() + " ");

        final String[] menuItemArray = KeyManage.getScreenNames("@", "");
        final AlertDialog.Builder accountDlg = new AlertDialog.Builder(context);
        accountDlg.setTitle(context.getString(R.string.tweet_reply) + ": " + ViewString.getScreennameAndText(status));
        accountDlg.setView(editText);
        accountDlg.setItems(
                menuItemArray,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if ((editText != null) && (!editText.getText().toString().equals(""))) {
                            try {
                                final TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                final StatusUpdate statusUpdate = new StatusUpdate(editText.getText().toString());
                                statusUpdate.inReplyToStatusId(status.getId());
                                twitterAccess.updateStatus(menuItemArray[which].replace("@", ""), statusUpdate);
                            } catch (Exception e) {
                                if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
                            }
                        }
                    }
                });
        accountDlg.create().show();
    }

    private static void tweetRetweet(final Status status) {
        final String[] menuItemArray = KeyManage.getScreenNames("@", "");
        final AlertDialog.Builder accountDlg = new AlertDialog.Builder(context);
        accountDlg.setTitle(context.getString(R.string.tweet_retweet) + ": " + ViewString.getScreennameAndText(status));
        accountDlg.setItems(
                menuItemArray,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int which) {
                        new Thread(new Runnable() {
                            @Override
                            public final void run() {
                                try {
                                    TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                    twitterAccess.retweet(menuItemArray[which], status);
                                } catch (Exception e) {
                                    if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
                                }
                            }
                        }).start();
                    }
                });
        accountDlg.create().show();
    }

    private static void tweetUserRetweet(final Status status) {
        final String[] menuItemArray = KeyManage.getScreenNames("@", "");
        final AlertDialog.Builder accountDlg = new AlertDialog.Builder(context);
        accountDlg.setTitle(context.getString(R.string.tweet_userretweet) + ": " + ViewString.getScreennameAndText(status));
        accountDlg.setItems(
                menuItemArray,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int which) {
                        new Thread(new Runnable() {
                            @Override
                            public final void run() {
                                try {
                                    TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                    twitterAccess.userRetweet(menuItemArray[which], status);
                                } catch (Exception e) {
                                    if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
                                }
                            }
                        }).start();
                    }
                });
        accountDlg.create().show();
    }

    private static void tweetCreateFavorite(final Status status) {
        final String[] menuItemArray = KeyManage.getScreenNames("@", "");
        final AlertDialog.Builder accountDlg = new AlertDialog.Builder(context);
        accountDlg.setTitle(context.getString(R.string.tweet_create_favorite) + ": " + ViewString.getScreennameAndText(status));
        accountDlg.setItems(
                menuItemArray,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int which) {
                        new Thread(new Runnable() {
                            @Override
                            public final void run() {
                                try {
                                    TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                    twitterAccess.createFavorite(menuItemArray[which], status);
                                } catch (Exception e) {
                                    if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
                                }
                            }
                        }).start();
                    }
                });
        accountDlg.create().show();
    }

    private static void tweetDestroyFavorite(final Status status) {
        final String[] menuItemArray = KeyManage.getScreenNames("@", "");
        final AlertDialog.Builder accountDlg = new AlertDialog.Builder(context);
        accountDlg.setTitle(context.getString(R.string.tweet_destroy_favorite) + ": " + ViewString.getScreennameAndText(status));
        accountDlg.setItems(
                menuItemArray,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int which) {
                        new Thread(new Runnable() {
                            @Override
                            public final void run() {
                                new Thread(new Runnable() {
                                    @Override
                                    public final void run() {
                                        try {
                                            TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                            twitterAccess.destroyFavorite(menuItemArray[which], status);
                                        } catch (Exception e) {
                                            if (pref_debug_write_logcat)
                                                Log.e("Yumura", e.getMessage());
                                        }
                                    }
                                }).start();
                            }
                        }).start();
                    }
                });
        accountDlg.create().show();
    }

    private static void tweetShareUrl(final Status status) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            String urlString = ViewString.getParmaLink(status);

            if (status.getURLEntities() != null) {
                if (status.getURLEntities().length > 0) {
                    URLEntity urlEntity = status.getURLEntities()[0];
                    urlString = urlEntity.getExpandedURL();
                }
            }

            intent.setData(Uri.parse(urlString));
            context.startActivity(intent);
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }
    }

    private static void tweetShareUrlTweet(final Status status) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(ViewString.getParmaLink(status)));
            context.startActivity(intent);
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }
    }

    private static void tweetShareText(final Status status) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, status.getText());
            context.startActivity(intent);
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }
    }

    private static void tweetDelete(final Status status) {

        final String[] menuItemArray = KeyManage.getScreenNames("@", "");
        final AlertDialog.Builder accountDlg = new AlertDialog.Builder(context);
        accountDlg.setTitle(context.getString(R.string.tweet_delete) + ": " + ViewString.getScreennameAndText(status));
        accountDlg.setItems(
                menuItemArray,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int which) {
                        new Thread(new Runnable() {
                            @Override
                            public final void run() {
                                try {
                                    TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                    twitterAccess.destroyStatus(menuItemArray[which], status);
                                } catch (Exception e) {
                                    if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
                                }
                            }
                        }).start();
                    }
                });
        accountDlg.create().show();
    }

    private static void tweetPak(final Status status) {
        final String[] menuItemArray = KeyManage.getScreenNames("@", "");
        final AlertDialog.Builder accountDlg = new AlertDialog.Builder(context);
        accountDlg.setTitle(context.getString(R.string.tweet_pak) + ": " + ViewString.getScreennameAndText(status));
        accountDlg.setItems(
                menuItemArray,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int which) {
                        new Thread(new Runnable() {
                            @Override
                            public final void run() {
                                try {
                                    TwitterAccess twitterAccess = new TwitterAccess(adapter);
                                    twitterAccess.pak(menuItemArray[which].replace("@", ""), status);
                                } catch (Exception e) {
                                    if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
                                }
                            }
                        }).start();
                    }
                });
        accountDlg.create().show();
    }

}
