package jp.gr.java_conf.ya.yumura; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved. --><!-- This software includes the work that is distributed in the Apache License 2.0

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.List;

import jp.gr.java_conf.ya.yumura.Cache.BitmapCache;
import jp.gr.java_conf.ya.yumura.Network.ImgGetter;
import jp.gr.java_conf.ya.yumura.Setting.PreferenceManage;
import jp.gr.java_conf.ya.yumura.String.ViewString;
import jp.gr.java_conf.ya.yumura.Twitter.BinarySearchUtil;
import jp.gr.java_conf.ya.yumura.Twitter.KeyManage;
import jp.gr.java_conf.ya.yumura.Twitter.TweetMenu;
import jp.gr.java_conf.ya.yumura.Twitter.TwitterAccess;
import twitter4j.ResponseList;
import twitter4j.Status;

public class TlAdapter extends RecyclerView.Adapter<TlAdapter.ViewHolder> {
    private boolean pref_debug_write_logcat = false;
    private boolean pref_tl_reverse_direction = false;

    private Context context;

    private float pref_tl_textsize_default = 12f;

    private ImageLoader mImageLoader;

    private int pref_tl_iconsize_default = 20;

    private LayoutInflater mLayoutInflater;

    private RecyclerView recyclerView;

    private SortedList<Status> mDataList;

    private String pref_tl_theme_list = "web";
    private int pref_tl_theme_color_background = Color.TRANSPARENT;
    private int pref_tl_theme_color_font = Color.TRANSPARENT;
    private String pref_icon_mute_screenname = "";
    private String pref_tl_move_to_unread_mute_source = "";
    private String pref_tl_mute_screenname = "";
    private String pref_tl_mute_text = "";

    private String[] muteTextArray;

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

    @Override
    public TlAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.content_tweet, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    public void addDataOf(final List<Status> dataList) {
        if (pref_debug_write_logcat) Log.i("Yumura", "addDataOf()");
        if (dataList != null) {
            try {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {
                        mDataList.addAll(dataList);
                    }
                });
            } catch (Exception e) {
                if (pref_debug_write_logcat) Log.e("Yumura", "addDataof(List<Status>) E: " + e.getMessage());
            }
        }
    }

    public void addDataOf(final Status data) {
        if (pref_debug_write_logcat) Log.i("Yumura", "addDataOf()");
        if (data != null) {
            try {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {
                        mDataList.add(data);
                    }
                });
            } catch (Exception e) {
                if (pref_debug_write_logcat) Log.e("Yumura", "addDataOf(Status) E: " + e.getMessage());
            }
        }
    }

    public void clearData() {
        try {
            mDataList.clear();
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", "clearData() E: " + e.getMessage());
        }
    }

    public Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        if (mDataList != null)
            return mDataList.size();
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if ((position < getItemCount()) && (position >= 0)) {
            try {
                return mDataList.get(position).getId();
            } catch (Exception e) {
                if (pref_debug_write_logcat) Log.e("Yumura", "getItemId() E: " + e.getMessage());
            }
        }

        return -1;
    }

    public SortedList<Status> getList() {
        return mDataList;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public int indexOf(Status status) {
        return mDataList.indexOf(status);
    }

    public final void moveToStartPositionOfReading() {
        if (pref_tl_reverse_direction) {
            // true: From bottom to top
            scrollTo(getItemCount() - 2);
        }
    }

    public final void moveToUnread() {
        if (pref_debug_write_logcat) {
            try {
                Log.i("Yumura", "id[0]: " + getItemId(0));
                Log.i("Yumura", "id[getItemCount()-1]: " + getItemId(getItemCount() - 1));
            } catch (Exception e) {
            }
        }
        try {
            long lastReadTweet = PreferenceManage.getLong(context, PreferenceManage.Last_Read_Tweet, 0);
            if (pref_debug_write_logcat)
                Log.i("Yumura", "lastReadTweet: " + Long.toString(lastReadTweet));
            if ((lastReadTweet > 0) && (getItemId(0) >= lastReadTweet) && (getItemId(getItemCount() - 2) <= lastReadTweet)) {
                final int position = BinarySearchUtil.binary_search_id(lastReadTweet, getList());
                scrollTo(position);
                return;
            } else {
                final Status status = TwitterAccess.getStatusJustBefore(KeyManage.getCurrentUser().screenName);
                if ((status != null) && (("," + pref_tl_move_to_unread_mute_source + ",").indexOf("," + status.getSource() + ",") == -1)) {
                    lastReadTweet = status.getId();
                    if ((lastReadTweet > 0) && (getItemId(0) >= lastReadTweet) && (getItemId(getItemCount() - 2) <= lastReadTweet)) {
                        final int position = BinarySearchUtil.binary_search_id(lastReadTweet, getList());
                        scrollTo(position);
                        return;
                    }
                } else {
                    ResponseList<Status> responseList = TwitterAccess.getStatusesJustBefore(KeyManage.getCurrentUser().screenName);
                    if (responseList != null) {
                        for (Status s : responseList) {
                            if ((s != null) && (("," + pref_tl_move_to_unread_mute_source + ",").indexOf("," + s.getSource() + ",") == -1)) {
                                lastReadTweet = s.getId();
                                if ((lastReadTweet > 0) && (getItemId(0) >= lastReadTweet) && (getItemId(getItemCount() - 2) <= lastReadTweet)) {
                                    final int position = BinarySearchUtil.binary_search_id(lastReadTweet, getList());
                                    scrollTo(position);
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            moveToStartPositionOfReading();
        } catch (Exception e) {
        }
    }

    @TargetApi(24)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Status status = mDataList.get(position);

        if (checkTlMute(status)) {
            holder.itemView.setVisibility(View.GONE);

            holder.statusText.setVisibility(View.GONE);
            holder.statusText.setText("");

            holder.statusIcon.setVisibility(View.GONE);
            holder.statusIcon.setImageResource(android.R.color.transparent);

            holder.statusIconRt.setVisibility(View.GONE);
            holder.statusIconRt.setImageResource(android.R.color.transparent);
        } else {
            holder.itemView.setVisibility(View.VISIBLE);

            holder.statusText.setVisibility(View.VISIBLE);

            final MovementMethod movementmethod = LinkMovementMethod.getInstance();
            holder.statusText.setMovementMethod(movementmethod);
            final ImgGetter imgGetter = new ImgGetter(holder.statusText, context);
            if (Build.VERSION.SDK_INT >= 24) {
                holder.statusText.setText(Html.fromHtml(ViewString.getStatusText(status), Html.FROM_HTML_MODE_LEGACY, imgGetter, null));
            } else {
                holder.statusText.setText(Html.fromHtml(ViewString.getStatusText(status), imgGetter, null));
            }
            holder.statusText.setTextSize(pref_tl_textsize_default);
            holder.statusText.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            saveLastReadTweet(status, position);
                        }
                    }
            );

            holder.statusIcon.setVisibility(View.VISIBLE);

            onBindViewHolderIcon(holder, status);
        }

        if (pref_tl_theme_color_background != Color.TRANSPARENT)
            holder.itemView.setBackgroundColor(pref_tl_theme_color_background);

        if (pref_tl_theme_color_font != Color.TRANSPARENT)
            holder.statusText.setTextColor(pref_tl_theme_color_font);
    }

    private boolean checkIconMute(Status status) {
        try {
            if (!pref_icon_mute_screenname.equals(",,")) {
                if (pref_icon_mute_screenname.indexOf("," + status.getUser().getScreenName() + ",") > -1)
                    return true;
                if (status.getRetweetedStatus() != null)
                    if (pref_icon_mute_screenname.indexOf("," + status.getRetweetedStatus().getUser().getScreenName() + ",") > -1)
                        return true;
            }
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", "checkIconMute() E: " + e.getMessage());
        }
        return false;
    }

    private boolean checkTlMute(Status status) {
        try {
            if ((muteTextArray != null) && (muteTextArray.length > 0) && (!muteTextArray[0].equals(""))) {
                for (final String muteItem : muteTextArray)
                    if (status.getText().indexOf(muteItem) > -1)
                        return true;
            }
            if (!pref_tl_mute_screenname.equals(",,")) {
                if (pref_tl_mute_screenname.indexOf("," + status.getUser().getScreenName() + ",") > -1)
                    return true;
                if (status.getRetweetedStatus() != null)
                    if (pref_tl_mute_screenname.indexOf("," + status.getRetweetedStatus().getUser().getScreenName() + ",") > -1)
                        return true;
            }
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", "checkTlMute() E: " + e.getMessage());
        }
        return false;
    }

    public void removeDataOf(List<Status> dataList) {
        mDataList.beginBatchedUpdates();
        for (Status data : dataList) {
            mDataList.remove(data);
        }
        mDataList.endBatchedUpdates();
    }

    public void scrollTo(final int pos) {
        try {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if ((0 <= pos) && (pos < getItemCount()))
                        recyclerView.smoothScrollToPosition(pos);
                }
            });
        } catch (Exception e) {
        }
    }

    public void showSnackbar(final String actionText, final String text) {
        try {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final FloatingActionButton fab = (FloatingActionButton) ((Activity) context).findViewById(R.id.fab);
                    if (fab != null)
                        Snackbar.make(fab, actionText + ": " + text, Snackbar.LENGTH_LONG)
                                .setAction(actionText, null).show();
                }
            });
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", "showSnackbar() E: " + e.getMessage());
        }
    }

    public void showToast(final String text) {
        try {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            if (pref_debug_write_logcat) Log.e("Yumura", "showToast() E: " + e.getMessage());
        }
    }

    private void getPreferences() {
        pref_debug_write_logcat = PreferenceManage.getBoolean(context, "pref_debug_write_logcat", pref_debug_write_logcat);
        pref_tl_iconsize_default = (int) (context.getResources().getDisplayMetrics().density *
                PreferenceManage.getInt(context, "pref_tl_iconsize_default",
                        (int) (context.getResources().getDisplayMetrics().density * pref_tl_iconsize_default)));
        pref_icon_mute_screenname = "," + PreferenceManage.getString(context, "pref_icon_mute_screenname", pref_icon_mute_screenname) + ",";
        pref_tl_move_to_unread_mute_source = PreferenceManage.getString(context, "pref_tl_move_to_unread_mute_source", pref_tl_move_to_unread_mute_source);
        pref_tl_mute_screenname = "," + PreferenceManage.getString(context, "pref_tl_mute_screenname", pref_tl_mute_screenname) + ",";
        pref_tl_mute_text = PreferenceManage.getString(context, "pref_tl_mute_text", pref_tl_mute_text);
        pref_tl_reverse_direction = PreferenceManage.getBoolean(context, "pref_tl_textsize_default", pref_tl_reverse_direction);
        pref_tl_textsize_default = PreferenceManage.getFloat(context, "pref_tl_textsize_default", pref_tl_textsize_default);

        if ((pref_tl_mute_text != null) && (!pref_tl_mute_text.equals("")))
            muteTextArray = pref_tl_mute_text.split(",");


        new Thread(new Runnable() {
            @Override
            public final void run() {
                pref_tl_theme_list = PreferenceManage.getString(context, "pref_tl_theme_list", pref_tl_theme_list);

                if ((pref_tl_theme_list != null) && (!pref_tl_theme_list.equals(""))) {
                    if (pref_tl_theme_list.equals("2")) {
                        pref_tl_theme_color_background = ContextCompat.getColor(context, android.R.color.background_dark);
                        pref_tl_theme_color_font = ContextCompat.getColor(context, android.R.color.secondary_text_dark);
                    } else if (pref_tl_theme_list.equals("1")) {
                        pref_tl_theme_color_background = ContextCompat.getColor(context, android.R.color.background_light);
                        pref_tl_theme_color_font = ContextCompat.getColor(context, android.R.color.secondary_text_light);
                    } else if (pref_tl_theme_list.equals("0")) {
                        final String[] colors = TwitterAccess.getColors();
                        pref_tl_theme_color_background = Color.parseColor(colors[0]);
                        pref_tl_theme_color_font = Color.parseColor(colors[1]);
                    }
                }
                if (pref_debug_write_logcat) {
                    Log.i("Yumura", "pref_tl_theme_list: " + pref_tl_theme_list);
                    Log.i("Yumura", "pref_tl_theme_color_background: " + pref_tl_theme_color_background);
                    Log.i("Yumura", "pref_tl_theme_color_font: " + pref_tl_theme_color_font);
                }
            }
        }).start();
    }

    private void onBindViewHolderIcon(ViewHolder holder, final Status status) {
        if (status.getRetweetedStatus() != null) {
            holder.statusIconRt.setVisibility(View.VISIBLE);
            if (checkIconMute(status)) {
                holder.statusIcon.setImageResource(android.R.color.transparent);
                holder.statusIconRt.setImageResource(android.R.color.transparent);
            } else {
                holder.statusIcon.setImageUrl(status.getRetweetedStatus().getUser().getProfileImageURLHttps(), mImageLoader);
                holder.statusIconRt.setImageUrl(status.getUser().getProfileImageURLHttps(), mImageLoader);
            }
        } else {
            holder.statusIconRt.setVisibility(View.INVISIBLE);
            if (checkIconMute(status)) {
                holder.statusIcon.setImageResource(android.R.color.transparent);
            } else {
                holder.statusIcon.setImageUrl(status.getUser().getProfileImageURLHttps(), mImageLoader);
            }
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

    private void saveLastReadTweet(final Status status, final int position) {
        if (pref_debug_write_logcat) Log.i("Yumura",
                "saveLastReadTweet(" + Long.toString(status.getId()) + ", " + Integer.toString(position) + ")");
        long lastReadTweet = PreferenceManage.getLong(context, PreferenceManage.Last_Read_Tweet, 0);
        if (lastReadTweet < status.getId()) {
            PreferenceManage.putLong(context, PreferenceManage.Last_Read_Tweet, status.getId());
            scrollTo(position);
            if (pref_debug_write_logcat)
                Log.i("Yumura", "Last_Read_Tweet putLong " + Long.toString(status.getId()) + " " + position);
        }
    }

    private static class SortedListCallback extends SortedList.Callback<Status> {

        private RecyclerView.Adapter adapter;

        public SortedListCallback(@NonNull RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public int compare(Status status1, Status status2) {
            if ((status1.getCreatedAt()).equals(status2.getCreatedAt())) {
                return 0;
            } else if ((status1.getCreatedAt()).compareTo(status2.getCreatedAt()) > 0) {
                return -1;
            } else {
                return 1;
            }
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
}