package jp.gr.java_conf.ya.yumura; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import org.junit.Test;

import jp.gr.java_conf.ya.yumura.Twitter.TwitterAccess;

import static org.junit.Assert.*;

public class UnitTest_TwitterAccess {
    @Test
    public void loadTimeline_isCorrect() throws Exception {
        final String arg = " https://twitter.com/https://twitter.com/#auth ";
        assertEquals(new String[]{"twitter.com/", "twitter.com/"}, (new TwitterAccess(null)).loadTimeline(arg, 200, -1L, -1, -1L));
    }
}