package com.example.laura.booklistingapp;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by Laura on 6/6/17.
 */

public class BookLoader extends AsyncTaskLoader<List<Book>> {

    String mUrl;
    Context mContext;

    public BookLoader (Context context, String queryUrl) {
        super(context);
        mContext = context;
        mUrl = queryUrl;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Book> loadInBackground() {
        Log.i("HERE", "LOAD IN BACKGROUND");

        if (mUrl == null || mUrl.length() == 0) {
            return null;
        }
        // Extracts the earthquake data from the query url
        return QueryUtils.fetchBookData(mContext, mUrl);
    }
}
