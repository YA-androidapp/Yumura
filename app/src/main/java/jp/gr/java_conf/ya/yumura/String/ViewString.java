package jp.gr.java_conf.ya.yumura.String; // Copyright (c) 2013-2017 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.util.Log;

import com.twitter.Autolink;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.gr.java_conf.ya.yumura.Twitter.TwitterAccess;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

public class ViewString {
    public static final SimpleDateFormat sdf_yyyyMMddHHmmss = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN);
    private static boolean pref_debug_write_logcat = true;

    private Autolink autolink = new Autolink();

    public static String getScreennameAndText(final Status status) {
        final StringBuilder sb = new StringBuilder();
        if (pref_debug_write_logcat)
            Log.i("Yumura", "getScreennameAndText() sb");
        try {
            sb.append("@").append(status.getUser().getScreenName()).append(": ")
                    .append(status.getText());
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }
        return sb.toString();
    }

    public static String getScreennameAndTextAndFooter(Status status) {
        status = getOriginalStatus(status);
        final StringBuilder sb = new StringBuilder();
        try {
            sb.append("@").append(status.getUser().getScreenName()).append(": ")
                    .append(status.getText()).append(" ")
                    .append(getTweetFooter(status, "", ""));
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }
        return sb.toString();
    }

    public static String getTextExpandedImg(String text, final Status status, final boolean pref_tl_img_show) {
        final MediaEntity[] extendedMediaEntities = status.getExtendedMediaEntities();
        for (int i = 0; i < extendedMediaEntities.length; i++) {
            final int pSizeKey = MediaEntity.Size.LARGE + 1;
            MediaEntity.Size pSize = null;
            String pMediaUrl = "";

            final MediaEntity mediaEntity = extendedMediaEntities[i];
            final Map<Integer, MediaEntity.Size> sizes = mediaEntity.getSizes();
            for (final Map.Entry<Integer, MediaEntity.Size> e : sizes.entrySet()) {
                final Integer sizeKey = e.getKey();
                if (sizeKey < pSizeKey) {
                    pSize = e.getValue();
                    pMediaUrl = getSizeMediaURL(mediaEntity.getMediaURL(), getSizeString(sizeKey));
                }
            }

            if (pref_tl_img_show) {
                final String link = getLinkedImg(mediaEntity.getExpandedURL(), pMediaUrl, pSize, mediaEntity.getDisplayURL());
                text = text + " " + link;
            } else {
                final String link = getLinkedUrl(mediaEntity);
                final String tco = mediaEntity.getURL();
                final Pattern p = Pattern.compile(tco);
                final Matcher m = p.matcher(text);
                text = m.replaceAll(link);
            }
        }

        return text;
    }

    public static String getTextExpandedTco(String text, final Status status) {
        final URLEntity[] entities = status.getURLEntities();
        if ((entities != null) && (entities.length > 0)) {
            for (final URLEntity entity : entities) {
                final String link = getLinkedUrl(entity);
                final String tco = entity.getURL();
                final Pattern p = Pattern.compile(tco);
                final Matcher m = p.matcher(text);
                text = m.replaceAll(link);
            }
        }

        return text;
    }

    public static String getStyledString(final String text, final String colorString) {
        if (!colorString.equals("")) {
            return "<font color=\"" + colorString + "\">" + text + "</font>";
        } else {
            return text;
        }
    }

    public static String getLinkedImg(final String href, final String src, final MediaEntity.Size size, final String displayUrl) {
        return "<a href=\"" + href + "\">" + displayUrl + "<img src=\"" + src + "\" width=\"" + Integer.toString(size.getWidth()) + "\" height=\"" + Integer.toString(size.getHeight()) + "\" ></a>";
    }

    public static String getLinkedUrl(final URLEntity entity) {
        return "<a href=\"" + entity.getExpandedURL() + "\">" + entity.getExpandedURL() + "</a>";
    }

    private static Status getOriginalStatus(final Status status) {
        try {
            return (status.getRetweetedStatus() != null) ? status.getRetweetedStatus() : status;
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
            return status;
        }
    }

    public static String getParmaLink(final Status status) {
        return TwitterAccess.URL_PROTOCOL + TwitterAccess.URL_TWITTER + "/" + status.getUser().getScreenName() + "/status/" + status.getId();
    }

    private static String getSizeMediaURL(final String mediaUrl, final String sizeString) {
        return mediaUrl + ":" + sizeString;
    }

    private static String getSizeString(final Integer sizeKey) {
        if (MediaEntity.Size.LARGE.equals(sizeKey)) {
            return "large";
        } else if (MediaEntity.Size.MEDIUM.equals(sizeKey)) {
            return "medium";
        } else if (MediaEntity.Size.SMALL.equals(sizeKey)) {
            return "small";
        } else if (MediaEntity.Size.THUMB.equals(sizeKey)) {
            return "thumb";
        } else {
            return "";
        }
    }

    public static String getTweetFooter(final Status status, final String pref_tl_fontcolor_favorite, final String pref_tl_fontcolor_retweet) {
        final StringBuilder sb = new StringBuilder();

        try {
            sb.append(sdf_yyyyMMddHHmmss.format(status.getCreatedAt())).append(" ");
            if (status.getRetweetCount() > 0)
                sb.append(getStyledString(status.getRetweetCount() + "RT", pref_tl_fontcolor_retweet)).append(" ");

            if (status.getFavoriteCount() > 0)
                sb.append(getStyledString(status.getFavoriteCount() + "Fav", pref_tl_fontcolor_favorite)).append(" ");

            sb.append(status.getSource().replaceAll("<[^>]+>", ""));
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }
        return sb.toString();
    }

    public String getStatusText(final Status status, final boolean pref_tl_img_show, final String pref_tl_fontcolor_favorite, final String pref_tl_fontcolor_retweet) {
        final Status originalStatus = getOriginalStatus(status);
        final StringBuilder sb = new StringBuilder();
        try {
            sb.append("@").append(originalStatus.getUser().getScreenName()).append(" <br>")
                    .append(getTextExpanded(originalStatus, pref_tl_img_show)).append("<br>")
                    .append(getTweetFooter(originalStatus, pref_tl_fontcolor_favorite, pref_tl_fontcolor_retweet));
            if (status.getRetweetedStatus() != null) {
                sb.append("<br>")
                        .append(getStyledString((sdf_yyyyMMddHHmmss.format(status.getCreatedAt()) + " RTed by @" + status.getUser().getScreenName()), pref_tl_fontcolor_retweet));
            }

            if (originalStatus.isFavorited())
                sb.append("<img src=\"favorite_on\">");

            if (originalStatus.isRetweetedByMe()) {
                sb.append("<img src=\"retweet_on\">");
            } else if (originalStatus.isRetweet()) {
                sb.append("<img src=\"retweet_hover\">");
            }
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }
        return sb.toString();
    }

    public String getTextExpanded(final Status status, final boolean pref_tl_img_show) {
        String text = status.getText();

        text = getTextExpandedTco(text, status);
        text = getTextExpandedImg(text, status, pref_tl_img_show);

        try {
            text = autolink.autoLinkUsernamesAndLists(text);
            text = autolink.autoLinkHashtags(text);
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
        }
        return text;
    }
}
