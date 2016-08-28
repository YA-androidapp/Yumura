package jp.gr.java_conf.ya.yumura.Twitter; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.ya.yumura.R;
import jp.gr.java_conf.ya.yumura.String.ViewString;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.URLEntity;

public class TweetMenu {
    private static Context context;

    public static void showTweetMenu(final Context c, final Status status) {
        context = c;
        final Status statusRT = (status.getRetweetedStatus() != null) ? status.getRetweetedStatus() : status;

        final String screennameAndText = ViewString.getScreennameAndText(status);

        final List<String> menuItem = new ArrayList<>();
        menuItem.add("@" + status.getUser().getScreenName());
        if (status.getRetweetedStatus() != null)
            menuItem.add("@" + status.getUser().getScreenName());
        menuItem.add(context.getString(R.string.tweet_reply));
        menuItem.add(context.getString(R.string.tweet_retweet));
        menuItem.add(context.getString(R.string.tweet_userretweet));
        menuItem.add(context.getString(R.string.tweet_favorite));
        menuItem.add(context.getString(R.string.tweet_favoriteretweet));
        menuItem.add(context.getString(R.string.tweet_favoriteuserretweet));
        menuItem.add(context.getString(R.string.tweet_share_url));
        menuItem.add(context.getString(R.string.tweet_share_url_tweet));
        menuItem.add(context.getString(R.string.tweet_share_text));
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
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_favorite))) {
                            tweetFavorite(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_favoriteretweet))) {
                            tweetFavorite(statusRT);
                            tweetRetweet(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_favoriteuserretweet))) {
                            tweetFavorite(statusRT);
                            tweetUserRetweet(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_share_url))) {
                            tweetShareUrl(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_share_url_tweet))) {
                            tweetShareUrlTweet(statusRT);
                        } else if (menuItemArray[which].equals(context.getString(R.string.tweet_share_text))) {
                            tweetShareText(statusRT);
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
        }
    }

    private static void tweetReply(final Status status) {
        try {
            final EditText editText = new EditText(context);
            editText.setHint(R.string.action_update_text);
            editText.setText("@" + status.getUser().getScreenName() + " ");

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.action_update);
            builder.setView(editText);
            builder.setPositiveButton(R.string.action_update,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if ((editText != null) && (!editText.getText().toString().equals(""))) {
                                TwitterAccess twitterAccess = new TwitterAccess(null);
                                StatusUpdate statusUpdate = new StatusUpdate(editText.getText().toString());
                                statusUpdate.inReplyToStatusId(status.getId());
                                twitterAccess.updateStatus(statusUpdate);
                            }
                        }
                    });
            builder.create().show();
        } catch (Exception e) {
        }
    }

    private static void tweetRetweet(final Status status) {
        try {
            TwitterAccess twitterAccess = new TwitterAccess(null);
            twitterAccess.retweet(status);
        } catch (Exception e) {
        }
    }

    private static void tweetUserRetweet(final Status status) {
        try {
            TwitterAccess twitterAccess = new TwitterAccess(null);
            twitterAccess.userRetweet(status);
        } catch (Exception e) {
        }
    }

    private static void tweetFavorite(final Status status) {
        try {
            TwitterAccess twitterAccess = new TwitterAccess(null);
            twitterAccess.favorite(status);
        } catch (Exception e) {
        }
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
        }
    }

    private static void tweetShareUrlTweet(final Status status) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(ViewString.getParmaLink(status)));
            context.startActivity(intent);
        } catch (Exception e) {
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
        }
    }

    private static void tweetPak(final Status status) {
        try {
            TwitterAccess twitterAccess = new TwitterAccess(null);
            twitterAccess.pak(status);
        } catch (Exception e) {
        }
    }

}
