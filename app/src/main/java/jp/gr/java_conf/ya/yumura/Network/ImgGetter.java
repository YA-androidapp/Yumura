package jp.gr.java_conf.ya.yumura.Network; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

// http://stackoverflow.com/questions/7424512/android-html-imagegetter-as-asynctask/7442725#7442725

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.gr.java_conf.ya.yumura.Setting.PreferenceManage;

public class ImgGetter implements Html.ImageGetter {
    View container;

    private float pref_tl_img_zoom = 3.0f;

    public ImgGetter(final View t, final Context c) {
        this.container = t;

        pref_tl_img_zoom = PreferenceManage.getFloat(c,"pref_tl_img_zoom", 3.0f);
    }

    public Drawable getDrawable(final String source) {
        final URLDrawable urlDrawable = new URLDrawable();
        final ImageGetterAsyncTask asyncTask =
                new ImageGetterAsyncTask(urlDrawable);
        asyncTask.execute(source);
        return urlDrawable;
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
            // set the correct bound according to the result from HTTP call
            urlDrawable.setBounds(0, 0, (int)(pref_tl_img_zoom * result.getIntrinsicWidth()), (int)(pref_tl_img_zoom * result.getIntrinsicHeight()));

            // change the reference of the current drawable to the result
            // from the HTTP call
            urlDrawable.drawable = result;

            // redraw the image by invalidating the container
            ImgGetter.this.container.invalidate();
        }

        public Drawable fetchDrawable(String urlString) {
            try {
                final InputStream is = fetch(urlString);
                final Drawable drawable = Drawable.createFromStream(is, "src");
                drawable.setBounds(0, 0, (int)(pref_tl_img_zoom * drawable.getIntrinsicWidth()), (int)(pref_tl_img_zoom * drawable.getIntrinsicHeight()));
                return drawable;
            } catch (Exception e) {
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
}
