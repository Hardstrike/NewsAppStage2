package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final int NEWS_LOADER_ID = 1;
    private static final String NEWS_API_URL = "http://content.guardianapis.com/search";
    private static final String NEWS_API_KEY = "3586eb9d-e1fe-49a9-91da-93e9986bbab5";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView.Adapter mAdapter;
    private TextView mEmptyTextView;
    private ProgressBar mLoadingSpinner;

    private ArrayList<News> mNewsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.news_recycler_view);
        mEmptyTextView = (TextView) findViewById(R.id.empty_text_view);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mNewsArrayList = new ArrayList<News>();
        mAdapter = new NewsAdapter(mNewsArrayList);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isConnected()) {
                    getLoaderManager().restartLoader(0, null, MainActivity.this);
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (mAdapter instanceof NewsAdapter) {
                        ((NewsAdapter) mAdapter).swapData(new ArrayList<News>());
                    }
                    mEmptyTextView.setText(R.string.not_connected);
                    mEmptyTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        if (isConnected()) {

            LoaderManager loaderManager = getLoaderManager();


            Log.d(LOG_TAG, "initLoader is called ...");
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            mLoadingSpinner.setVisibility(View.GONE);

            mEmptyTextView.setText(getString(R.string.not_connected));
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader is called ...");

        Date today = new Date();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        String section = sharedPrefs.getString(
                getString(R.string.settings_section_key),
                getString(R.string.settings_section_default));

        //Build URI using append for each parameter
        Uri baseUri = Uri.parse(NEWS_API_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("show-fields", "byline");
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("from-date", date);
        uriBuilder.appendQueryParameter("section", section);
        uriBuilder.appendQueryParameter("api-key", NEWS_API_KEY);
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> newsList) {
        Log.d(LOG_TAG, "onLoadFinished is called ...");

        mLoadingSpinner.setVisibility(View.GONE);

        mSwipeRefreshLayout.setRefreshing(false);

        mEmptyTextView.setText(getString(R.string.nothing));
        if (newsList != null && !newsList.isEmpty()) {
            mEmptyTextView.setVisibility(View.GONE);
        } else {
            mEmptyTextView.setVisibility(View.VISIBLE);
        }
        if (mAdapter instanceof NewsAdapter) {
            ((NewsAdapter) mAdapter).swapData(newsList);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        Log.d(LOG_TAG, "onLoaderReset is called ...");

        mNewsArrayList.clear();
        if (mAdapter instanceof NewsAdapter) {
            ((NewsAdapter) mAdapter).swapData(mNewsArrayList);
        }
    }
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
