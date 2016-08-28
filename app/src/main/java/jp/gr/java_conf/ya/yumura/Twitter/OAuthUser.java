package jp.gr.java_conf.ya.yumura.Twitter; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

public class OAuthUser {
    public String alias;
    public String consumerKey;
    public String consumerSecret;
    public String screenName;
    public String token;
    public String tokenSecret;
    public long userId;

    public String toLogString(){
        return alias + " , "+consumerKey+ " , "+consumerSecret+ " , "+screenName+ " , "+token+ " , "+tokenSecret+ " , "+Long.toString(userId);
    }

    public OAuthUser(final String alias,final String consumerKey, final String consumerSecret, final String screenName, final String token, final String tokenSecret, final long userId) {
        this.alias = alias;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.screenName = screenName;
        this.token = token;
        this.tokenSecret = tokenSecret;
        this.userId = userId;
    }
}