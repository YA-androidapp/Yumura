package jp.gr.java_conf.ya.yumura.String; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.support.v7.util.SortedList;

import twitter4j.Status;

public class SearchStatus {
    public static final int searchStatusPositionByTweetScreenname(final String searchStr, SortedList<Status> tweets, int position) {
        for (int pos = (position + 1 < tweets.size()) ? position + 1 : 0; pos < tweets.size(); pos++) {
            if (tweets.get(pos).getUser().getScreenName().indexOf(searchStr) > -1)
                return pos;
        }
        return -1;
    }

    public static final int searchStatusPositionByTweetSource(final String searchStr, SortedList<Status> tweets, int position) {
        for (int pos = (position + 1 < tweets.size()) ? position + 1 : 0; pos < tweets.size(); pos++) {
            if (tweets.get(pos).getSource().indexOf(searchStr) > -1)
                return pos;
        }
        return -1;
    }

    public static final int searchStatusPositionByTweetText(final String searchStr, SortedList<Status> tweets, int position) {
        for (int pos = (position + 1 < tweets.size()) ? position + 1 : 0; pos < tweets.size(); pos++) {
            if (tweets.get(pos).getText().indexOf(searchStr) > -1)
                return pos;
        }
        return -1;
    }

}
