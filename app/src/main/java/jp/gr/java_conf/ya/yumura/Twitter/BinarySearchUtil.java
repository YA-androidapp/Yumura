package jp.gr.java_conf.ya.yumura.Twitter; // Copyright (c) 2013-2017 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.support.v7.util.SortedList;

import twitter4j.Status;

public final class BinarySearchUtil {
    public static final int binary_search_time(final long needle, final SortedList<Status> tweets, int position) {
        int low = (position > -1) ? position : 0;
        int high = (tweets.size() > 1) ? (tweets.size() - 1) : 0;
        int mid = (low + high) / 2, preMid = -1;
        while (low <= high) {
            mid = (low + high) / 2;
            if (mid == preMid) {
                return mid;
            } else if (needle > tweets.get(mid).getCreatedAt().getTime()) {
                high = mid + 1;
            } else {
                low = mid - 1;
            }
            preMid = mid;
        }
        return mid;
    }

    public static final int binary_search_id(final long needle, final SortedList<Status> tweets) {
        int low = 0;
        int high = tweets.size() - 1;
        int mid = (low + high) / 2;

        while (low <= high) {
            mid = (low + high) / 2;
            if (needle == tweets.get(mid).getId()) {
                return mid;
            } else if (needle > tweets.get(mid).getId()) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return mid;
    }
}
