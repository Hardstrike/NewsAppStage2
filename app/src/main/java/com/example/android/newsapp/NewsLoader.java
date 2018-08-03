package com.example.android.newsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NewsLoader extends AsyncTaskLoader<List<News>> {
    private static final String LOG_TAG = NewsLoader.class.getName();

    private final String mUrl;

    public NewsLoader(Context context, String url) {
        super(context);
        this.mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        Log.d(LOG_TAG, "onStartLoading is called ...");

        forceLoad();
    }

    @Override
    public List<News> loadInBackground() {
        Log.d(LOG_TAG, "loadInBackground is called ...");

        if (TextUtils.isEmpty(mUrl)) {
            return null;
        }
        List<News> newsList = Query.fetchNewsData(mUrl);
        return newsList;
    }
}
