package jp.gr.java_conf.ya.yumura.Cache; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import java.util.LinkedHashMap;
import java.util.Map;

// http://qiita.com/yoshi389111/items/230ce4f6f3d9ee5f6867
public class LruCacheMap<K, V> extends LinkedHashMap<K, V> {
    /**
     * シリアライズバージョン
     */
    private static final long serialVersionUID = 1L;

    /**
     * キャッシュエントリ最大数
     */
    private final int maxSize;

    /**
     * 指定された最大数でインスタンスを生成
     *
     * @param maxSize 最大数
     */
    public LruCacheMap(int maxSize) {
        super(15, 0.75f, true);
        this.maxSize = maxSize;
    }

    /**
     * エントリの削除要否を判断
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}