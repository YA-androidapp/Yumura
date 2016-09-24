package jp.gr.java_conf.ya.yumura.Network; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

// http://stackoverflow.com/questions/7424512/android-html-imagegetter-as-asynctask/7442725#7442725

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.gr.java_conf.ya.yumura.Setting.PreferenceManage;

public class ImgGetter implements Html.ImageGetter {
    View container;
    Context context;

    private static boolean pref_debug_write_logcat = true;
    private static float pref_tl_img_zoom = 3.0f;

    public ImgGetter(final View t, final Context c) {
        this.container = t;
        this.context = c;

        pref_tl_img_zoom = PreferenceManage.getFloat(c, "pref_tl_img_zoom", 1.0f);
    }

    public Drawable getDrawable(final String source) {
        if ((source.equals("favorite")) || (source.equals("favorite_hover")) || (source.equals("favorite_on")) || (source.equals("reply")) || (source.equals("reply_hover"))
                || (source.equals("retweet")) || (source.equals("retweet_hover")) || (source.equals("retweet_on"))) {
            try {
                final int id = context.getResources().getIdentifier(source, "drawable", context.getPackageName());
                final Drawable drawable = context.getResources().getDrawable(id);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                return drawable;
            } catch (final Exception e) {
                if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
            }
            return context.getResources().getDrawable(android.R.drawable.ic_delete);
        } else {
            final URLDrawable urlDrawable = new URLDrawable();
            final ImageGetterAsyncTask asyncTask =
                    new ImageGetterAsyncTask(urlDrawable);
            asyncTask.execute(source);
            return urlDrawable;
        }
    }

    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            urlDrawable.setBounds(0, 0, (int) (pref_tl_img_zoom * result.getIntrinsicWidth()), (int) (pref_tl_img_zoom * result.getIntrinsicHeight()));
            urlDrawable.drawable = result;

            ImgGetter.this.container.invalidate();
        }

        public Drawable fetchDrawable(String urlString) {
            try {
                final InputStream is = fetch(urlString);
                final Drawable drawable = Drawable.createFromStream(is, "src");
                drawable.setBounds(0, 0, (int) (pref_tl_img_zoom * drawable.getIntrinsicWidth()), (int) (pref_tl_img_zoom * drawable.getIntrinsicHeight()));
                drawable.setAlpha(50);
                return drawable;
            } catch (Exception e) {
                if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
                return null;
            }
        }

        private InputStream fetch(String urlString) throws IOException {
            final URL url = new URL(urlString);
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            final InputStream stream = urlConnection.getInputStream();

            return stream;
        }
    }

    public static Drawable fetchDrawable(final String urlString, final int width, final int height) {
        try {
            final InputStream is = fetch(urlString);
            final Drawable drawable = Drawable.createFromStream(is, "src");
            drawable.setBounds(0, 0, width, height);
            drawable.setAlpha(50);
            return drawable;
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", e.getMessage());
            return null;
        }
    }

    private static InputStream fetch(String urlString) throws IOException {
        final URL url = new URL(urlString);
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        final InputStream stream = urlConnection.getInputStream();

        return stream;
    }
}
