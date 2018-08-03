package com.example.android.newsapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Query {
    private static final String LOG_TAG = Query.class.getSimpleName();
    private static final int URL_CONNECTION_READ_TIMEOUT = 10000;
    private static final int URL_CONNECTION_CONNECT_TIMEOUT = 15000;

    public static List<News> fetchNewsData(String requestUrl) {
        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        List<News> newsList = extractNews(jsonResponse);

        return newsList;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        Log.d(LOG_TAG, url.toString());

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(URL_CONNECTION_READ_TIMEOUT);
            urlConnection.setConnectTimeout(URL_CONNECTION_CONNECT_TIMEOUT);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            int httpResponse = urlConnection.getResponseCode();
            if (httpResponse == 301) {
                String newUrl = urlConnection.getHeaderField("Location");

                urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();

                Log.d(LOG_TAG, "Redirect to URL : " + newUrl);
            }


            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    @NonNull
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    @Nullable
    private static List<News> extractNews(String newsJSON) {

        List<News> newsList = new ArrayList<>();

        if (TextUtils.isEmpty(newsJSON)) {
            Log.d(LOG_TAG, "List of news is empty");
            return null;
        }

        try {

            JSONObject root = new JSONObject(newsJSON);
            JSONObject response = root.getJSONObject("response");

            if (response.has("results")) {
                JSONArray results = response.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {

                    JSONObject news = results.getJSONObject(i);

                    String title = news.getString("webTitle");

                    String section = news.getString("sectionName");

                    JSONObject fields = news.getJSONObject("fields");
                    String author;
                    if (fields.has("byline")) {
                        author = fields.getString("byline");
                    } else {
                        author = "";
                    }

                    String date;
                    if (news.has("webPublicationDate")) {
                        date = news.getString("webPublicationDate");
                    } else {
                        date = "";
                    }

                    String url = news.getString("webUrl");


                    newsList.add(new News(title, section, author, date, url));
                }
            }
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the news JSON results", e);
        }

        return newsList;
    }
}
