package jp.gr.java_conf.ya.yumura; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.List;

import jp.gr.java_conf.ya.yumura.Cache.BitmapCache;
import jp.gr.java_conf.ya.yumura.Network.ImgGetter;
import jp.gr.java_conf.ya.yumura.Setting.PreferenceManage;
import jp.gr.java_conf.ya.yumura.String.ViewString;
import jp.gr.java_conf.ya.yumura.Twitter.TweetMenu;
import twitter4j.Status;

public class TlAdapter extends RecyclerView.Adapter<TlAdapter.ViewHolder> {
    private Context context;
    private RecyclerView recyclerView;
    private SortedList<Status> mDataList;
    private ImageLoader mImageLoader;
    private LayoutInflater mLayoutInflater;

    private boolean pref_tl_reverse_direction = false;
    private int pref_tl_iconsize_default = 20;
    private float pref_tl_textsize_default = 12f;

    public TlAdapter(final Context context, final RecyclerView recyclerView) {
        super();

        this.context = context;
        this.recyclerView = recyclerView;
        getPreferences();

        mLayoutInflater = LayoutInflater.from(context);
        mDataList = new SortedList<>(Status.class, new SortedListCallback(this));

        ImageLoader.ImageCache imageCache = BitmapCache.getInstance();
        mImageLoader = new ImageLoader(Volley.newRequestQueue(context), imageCache);
    }

    public void getPreferences() {
        pref_tl_reverse_direction = PreferenceManage.getBoolean(context, "pref_tl_textsize_default", pref_tl_reverse_direction);
        pref_tl_iconsize_default = (int) (context.getResources().getDisplayMetrics().density *
                PreferenceManage.getInt(context, "pref_tl_iconsize_default",
                        (int) (context.getResources().getDisplayMetrics().density * pref_tl_iconsize_default)));
        pref_tl_textsize_default = PreferenceManage.getFloat(context, "pref_tl_textsize_default", pref_tl_textsize_default);
    }

    @Override
    public TlAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.content_tweet, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void addDataOf(List<Status> dataList) {

        mDataList.addAll(dataList);

        if (recyclerView != null) {
            if (pref_tl_reverse_direction) {
                // true: From bottom to top
                int firstVisiblePosition = 0;
                try {
                    firstVisiblePosition = ((LinearLayoutManager) (recyclerView.getLayoutManager())).findFirstVisibleItemPosition();
                } catch (Exception e) {
                }
                try {
                    recyclerView.smoothScrollToPosition(firstVisiblePosition + dataList.size() - 1);
                } catch (Exception e) {
                }
            }
        }
    }

    public void removeDataOf(List<Status> dataList) {
        mDataList.beginBatchedUpdates();
        for (Status data : dataList) {
            mDataList.remove(data);
        }
        mDataList.endBatchedUpdates();
    }

    public void clearData() {
        try {
            mDataList.clear();
        } catch (Exception e) {
        }
    }

    public SortedList<Status> getList() {
        return mDataList;
    }

    private void onBindViewHolderIcon(ViewHolder holder, final Status status) {
        if (status.getRetweetedStatus() != null) {
            holder.statusIcon.setImageUrl(status.getRetweetedStatus().getUser().getProfileImageURLHttps(), mImageLoader);
            holder.statusIconRt.setVisibility(View.VISIBLE);
            holder.statusIconRt.setImageUrl(status.getUser().getProfileImageURLHttps(), mImageLoader);
        } else {
            holder.statusIcon.setImageUrl(status.getUser().getProfileImageURLHttps(), mImageLoader);
            holder.statusIconRt.setVisibility(View.INVISIBLE);
            holder.statusIconRt.setImageResource(android.R.color.transparent);
        }
        final android.widget.LinearLayout.LayoutParams layoutParams = new android.widget.LinearLayout.LayoutParams(pref_tl_iconsize_default / 2, pref_tl_iconsize_default / 2);
        layoutParams.setMarginStart(5 + pref_tl_iconsize_default / 2);

        holder.statusIcon.setDefaultImageResId(android.R.drawable.ic_menu_recent_history);
        holder.statusIcon.setErrorImageResId(android.R.drawable.stat_sys_warning);
        holder.statusIcon.setLayoutParams(new android.widget.LinearLayout.LayoutParams(pref_tl_iconsize_default, pref_tl_iconsize_default));
        holder.statusIconRt.setDefaultImageResId(android.R.drawable.ic_menu_recent_history);
        holder.statusIconRt.setErrorImageResId(android.R.drawable.stat_sys_warning);
        holder.statusIconRt.setLayoutParams(layoutParams);

        holder.statusIcon.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        TweetMenu.showTweetMenu(context, status);
                    }
                }
        );
    }

    @TargetApi(24)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Status status = mDataList.get(position);

        final MovementMethod movementmethod = LinkMovementMethod.getInstance();
        holder.statusText.setMovementMethod(movementmethod);
        final ImgGetter imgGetter = new ImgGetter(holder.statusText, context);
        if (Build.VERSION.SDK_INT >= 24) {
            holder.statusText.setText(Html.fromHtml(ViewString.getStatusText(status), Html.FROM_HTML_MODE_LEGACY, imgGetter, null));
        } else {
            holder.statusText.setText(Html.fromHtml(ViewString.getStatusText(status), imgGetter, null));
        }
        holder.statusText.setTextSize(pref_tl_textsize_default);

        onBindViewHolderIcon(holder, status);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        com.android.volley.toolbox.NetworkImageView statusIcon;
        com.android.volley.toolbox.NetworkImageView statusIconRt;
        TextView statusText;

        public ViewHolder(View v) {
            super(v);
            statusIcon = (com.android.volley.toolbox.NetworkImageView) v.findViewById(R.id.statusIcon);
            statusIconRt = (com.android.volley.toolbox.NetworkImageView) v.findViewById(R.id.statusIconRt);
            statusText = (TextView) v.findViewById(R.id.statusText);
        }
    }

    private static class SortedListCallback extends SortedList.Callback<Status> {

        private RecyclerView.Adapter adapter;

        public SortedListCallback(@NonNull RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public int compare(Status data1, Status data2) {
            return -1 * Long.compare(data1.getId(), data2.getId());
        }

        @Override
        public void onInserted(int position, int count) {
            adapter.notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            adapter.notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            adapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            adapter.notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(Status oldData, Status newData) {
            String oldText = oldData.getText();
            if (oldText == null) {
                return newData.getText() == null;
            }
            return oldText.equals(newData.getText());
        }

        @Override
        public boolean areItemsTheSame(Status oldData, Status newData) {
            return oldData.getId() == newData.getId();
        }
    }
}