package jp.gr.java_conf.ya.yumura.Cache; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

public class BitmapCache implements ImageLoader.ImageCache{
    private LruCache<String, Bitmap> mMemoryCache;

    public static BitmapCache getInstance() {
        return new BitmapCache(getDefaultLruCacheSize());
    }

    private BitmapCache(int memCacheSize) {
        init(memCacheSize);
    }

    private void init(int memCacheSize) {
        mMemoryCache = new LruCache<String, Bitmap>(memCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                final int bitmapSize = getBitmapSize(bitmap) / 1024;
                return bitmapSize == 0 ? 1 : bitmapSize;
            }
        };
    }

    public static int getBitmapSize(Bitmap bitmap) {
        return bitmap.getByteCount();
    }

    @Override
    public Bitmap getBitmap(String key) {
        return getBitmapFromMemCache(key);
    }

    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        addBitmapToBothCache(key, bitmap);
    }

    public void addBitmapToBothCache(String key, Bitmap bitmap) {
        if (key == null || bitmap == null)
            return;

        synchronized (mMemoryCache) {
            if (mMemoryCache.get(key) == null)
                mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        if (key != null) {
            synchronized (mMemoryCache) {
                final Bitmap memBitmap = mMemoryCache.get(key);
                if (memBitmap != null)
                    return memBitmap;
            }
        }
        return null;
    }

    public void clearCache() {
        if (mMemoryCache != null)
            mMemoryCache.evictAll();
    }

    public static int getDefaultLruCacheSize() {
        return (int)(Runtime.getRuntime().maxMemory() / 8192);
    }
}