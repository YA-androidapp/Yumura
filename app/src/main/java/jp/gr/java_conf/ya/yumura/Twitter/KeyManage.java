package jp.gr.java_conf.ya.yumura.Twitter; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import jp.gr.java_conf.ya.yumura.App;
import jp.gr.java_conf.ya.yumura.String.MyCrypt;

public class KeyManage {
    private static boolean pref_debug_write_logcat = false;
    private static MyCrypt myCrypt;
    private static OAuthUser currentUser;
    private static SharedPreferences keyPreferences;

    private static String KEY_PREFERENCE_NAME = "YumuraKeyManage";

    public static String[] loadCurrentConsumerKeyAndSecret() {
        if (keyPreferences == null)
            keyPreferences = App.getContext().getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);

        final String currentConsumerKeyEncrypted = keyPreferences.getString("currentConsumerKey", "");
        final String currentConsumerSecretEncrypted = keyPreferences.getString("currentConsumerSecret", "");
        myCrypt = new MyCrypt();
        final String currentConsumerKey = myCrypt.decrypt(currentConsumerKeyEncrypted);
        final String currentConsumerSecret = myCrypt.decrypt(currentConsumerSecretEncrypted);
        return new String[]{currentConsumerKey, currentConsumerSecret};
    }

    public static void saveCurrentConsumerKeyAndSecret(final String currentConsumerKey, final String currentConsumerSecret) {
        if (keyPreferences == null)
            keyPreferences = App.getContext().getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);

        final SharedPreferences.Editor edit = keyPreferences.edit();
        myCrypt = new MyCrypt();
        final String currentConsumerKeyEncrypted = myCrypt.encrypt(currentConsumerKey);
        final String currentConsumerSecretEncrypted = myCrypt.encrypt(currentConsumerSecret);
        try {
            edit.putString("currentConsumerKey", currentConsumerKeyEncrypted);
            edit.putString("currentConsumerSecret", currentConsumerSecretEncrypted);
            edit.commit();
        } catch (Exception e) {
        }
    }

    public static int getUserCount() {
        if (getUserIds() == null) {
            return 0;
        } else {
            return getUserIds().length;
        }
    }

    public static OAuthUser getCurrentUser() {
        if (currentUser != null) {
            return currentUser;
        } else if (getUserCount() > 0) {
            return getUsers()[0];
        }
        return null;
    }

    public static void setCurrentUser(final long userId) {
        if (userId > -1) {
            setCurrentUser(getUser(userId));
        }
    }

    public static void setCurrentUser(final OAuthUser oauthUser) {
        if (oauthUser != null) {
            currentUser = oauthUser;
        }
    }

    public static void addUser(final OAuthUser ou) {
        if (keyPreferences == null)
            keyPreferences = App.getContext().getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);

        final SharedPreferences.Editor edit = keyPreferences.edit();
        final String userId = Long.toString(ou.userId);

        myCrypt = new MyCrypt();
        final String aliasEncrypted = myCrypt.encrypt(ou.alias);
        final String consumerKeyEncrypted = myCrypt.encrypt(ou.consumerKey);
        final String consumerSecretEncrypted = myCrypt.encrypt(ou.consumerSecret);
        final String screenNameEncrypted = myCrypt.encrypt(ou.screenName);
        final String tokenEncrypted = myCrypt.encrypt(ou.token);
        final String tokenSecretEncrypted = myCrypt.encrypt(ou.tokenSecret);
        final String userIdEncrypted = myCrypt.encrypt(userId);

        try {
            edit.putString(userId + ".alias", aliasEncrypted);
            edit.putString(userId + ".consumerKey", consumerKeyEncrypted);
            edit.putString(userId + ".consumerSecret", consumerSecretEncrypted);
            edit.putString(userId + ".screenName", screenNameEncrypted);
            edit.putString(userId + ".token", tokenEncrypted);
            edit.putString(userId + ".tokenSecret", tokenSecretEncrypted);
            edit.putString(userId + ".userId", userIdEncrypted);
            edit.commit();

            setCurrentUser(ou.userId);
        } catch (Exception e) {
            try {
                removeUser(ou.userId);
            } catch (Exception ex) {
            }
        }
    }

    public static OAuthUser getUser(final long userId) {
        if (keyPreferences == null)
            keyPreferences = App.getContext().getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);

        final String userIdStr = Long.toString(userId);
        final String aliasEncrypted = keyPreferences.getString(userIdStr + ".alias", "");
        final String consumerKeyEncrypted = keyPreferences.getString(userIdStr + ".consumerKey", "");
        final String consumerSecretEncrypted = keyPreferences.getString(userIdStr + ".consumerSecret", "");
        final String screenNameEncrypted = keyPreferences.getString(userIdStr + ".screenName", "");
        final String tokenEncrypted = keyPreferences.getString(userIdStr + ".token", "");
        final String tokenSecretEncrypted = keyPreferences.getString(userIdStr + ".tokenSecret", "");
        final String userIdEncryptedFromPref = keyPreferences.getString(userIdStr + ".userId", "-1");

        myCrypt = new MyCrypt();
        final String alias = myCrypt.decrypt(aliasEncrypted);
        final String consumerKey = myCrypt.decrypt(consumerKeyEncrypted);
        final String consumerSecret = myCrypt.decrypt(consumerSecretEncrypted);
        final String screenName = myCrypt.decrypt(screenNameEncrypted);
        final String token = myCrypt.decrypt(tokenEncrypted);
        final String tokenSecret = myCrypt.decrypt(tokenSecretEncrypted);
        final String userIdFromPref = myCrypt.decrypt(userIdEncryptedFromPref);

        if ((!alias.equals("")) && (!consumerKey.equals("")) && (!consumerSecret.equals("")) && (!screenName.equals("")) && (!token.equals("")) && (!tokenSecret.equals("")) && (!userIdFromPref.equals("-1"))) {
            return new OAuthUser(alias, consumerKey, consumerSecret, screenName, token, tokenSecret, userId);
        } else {
            return null;
        }
    }

    public static String[] getScreenNames(final String prefix, final String suffix) { // , final boolean addAllAccount) {
        if (keyPreferences == null)
            keyPreferences = App.getContext().getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);

        final ArrayList<String> ids = new ArrayList<>();
        final Map<String, ?> map = keyPreferences.getAll();
        if (pref_debug_write_logcat) Log.v("Yumura", "getAll");

        if ((map == null) || (map.isEmpty())) {
            if (pref_debug_write_logcat) Log.v("Yumura", "null");
            return null;
        } else {
            if (pref_debug_write_logcat) Log.v("Yumura", "!null");
            // if(addAllAccount)
            //     ids.add("All Accounts");

            myCrypt = new MyCrypt();
            if (pref_debug_write_logcat) Log.v("Yumura", "myCrypt");

            for (final Map.Entry<String, ?> entry : map.entrySet()) {
                final String key = entry.getKey();
                if (pref_debug_write_logcat) Log.v("Yumura", "key: " + key);
                if (key.contains("screenName")) {
                    try {
                        final String valueEncrypted = (String) entry.getValue();
                        if (pref_debug_write_logcat)
                            Log.v("Yumura", "valueEncrypted: " + valueEncrypted);
                        final String valueDecrypted = myCrypt.decrypt(valueEncrypted);
                        if (pref_debug_write_logcat)
                            Log.v("Yumura", "valueDecrypted: " + valueDecrypted);
                        final String value = prefix + valueDecrypted + suffix;
                        if (pref_debug_write_logcat) Log.v("Yumura", "value: " + value);
                        ids.add(value);
                    } catch (Exception e) {
                        if (pref_debug_write_logcat) Log.v("Yumura", "e: " + e.getMessage());
                    }
                }
            }
            return ids.toArray(new String[0]);
        }
    }

    public static Long[] getUserIds() {
        if (keyPreferences == null)
            keyPreferences = App.getContext().getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);

        final ArrayList<Long> ids = new ArrayList<>();
        final Map<String, ?> map = keyPreferences.getAll();

        if ((map == null) || (map.isEmpty())) {
            return null;
        } else {
            myCrypt = new MyCrypt();

            for (final Map.Entry<String, ?> entry : map.entrySet()) {
                final String key = entry.getKey();
                if (key.contains("userId")) {
                    try {
                        final String valueEncrypted = (String) entry.getValue();
                        final String valueDecrypted = myCrypt.decrypt(valueEncrypted);
                        final long value = Long.parseLong(valueDecrypted);
                        ids.add(value);
                    } catch (Exception e) {
                    }
                }
            }
            return ids.toArray(new Long[0]);
        }
    }

    public static long getIdFromScreenNameAtPreference(String screenName) {
        final Long[] ids = getUserIds();

        if ((ids == null) || (ids.length == 0)) {
            return -1;
        } else {
            for (final long id : ids) {
                if (getUser(id).screenName.toLowerCase(Locale.US).equals(screenName.toLowerCase(Locale.US))) {
                    return id;
                }
            }
            return -1;
        }
    }

    public static OAuthUser[] getUsers() {
        final ArrayList<OAuthUser> users = new ArrayList<>();
        final Long[] ids = getUserIds();

        if ((ids == null) || (ids.length == 0)) {
            return null;
        } else {
            for (final long id : ids) {
                users.add(getUser(id));
            }
            return users.toArray(new OAuthUser[0]);
        }
    }

    public static boolean isAuthenticatedUser(String screenName) {
        String[] authenticatedScreennames = getScreenNames("", "");
        for (String authenticatedScreenname : authenticatedScreennames) {
            if ((authenticatedScreenname.equals(screenName)) && (authenticatedScreenname.equals("")))
                return true;
        }

        return false;
    }

    public static void removeUser(final long userId) {
        if (keyPreferences == null)
            keyPreferences = App.getContext().getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);

        final SharedPreferences.Editor edit = keyPreferences.edit();
        final String userIdStr = Long.toString(userId);
        edit.remove(userIdStr + ".alias");
        edit.remove(userIdStr + ".consumerKey");
        edit.remove(userIdStr + ".consumerSecret");
        edit.remove(userIdStr + ".screenName");
        edit.remove(userIdStr + ".token");
        edit.remove(userIdStr + ".tokenSecret");
        edit.remove(userIdStr + ".userId");
        edit.commit();
    }
}
