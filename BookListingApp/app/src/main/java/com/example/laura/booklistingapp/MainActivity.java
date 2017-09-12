package com.example.laura.booklistingapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    public static final int BOOK_LOADER_ID = 1;
    public static final String QUERY_FORMAT = "https://www.googleapis.com/books/v1/volumes?q=%s&maxResults=%d";
    public static final String LOG_TAG = "MainActivity";
    public static final int DEFAULT_RESULTS = 10;

    private BookAdapter mAdapter;
    private String queryUrl;

    private TextView mEmptyStateTextView;
    private ProgressBar mProgressIndicator;
    private TextView mTotalResults;
    private EditText mShowResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load progress bar
        mProgressIndicator = (ProgressBar) findViewById(R.id.progress);
        mProgressIndicator.setVisibility(View.GONE);

        // Set adapter
        ListView listView = (ListView) findViewById(R.id.list_view);
        mAdapter = new BookAdapter(this, new ArrayList<Book>());
        listView.setAdapter(mAdapter);

        // Sets empty view
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        listView.setEmptyView(mEmptyStateTextView);
        mEmptyStateTextView.setText(R.string.empty);

        // Find text views for number of results
        mTotalResults = (TextView) findViewById(R.id.total_results);
        mTotalResults.setText(getString(R.string.total_results, 0));
        mShowResults = (EditText) findViewById(R.id.number_results);
        mShowResults.setHint(String.valueOf(DEFAULT_RESULTS));

        // Initiates loader to mantain information
        queryUrl = null;
        getLoaderManager().initLoader(BOOK_LOADER_ID, null, MainActivity.this);

        // Get query words
        final SearchView searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                int resultsRequested;
                if (mShowResults.getText().toString().isEmpty()) {
                    resultsRequested = DEFAULT_RESULTS;
                } else {
                    resultsRequested = Integer.parseInt(mShowResults.getText().toString());
                    if (resultsRequested > 40) {
                        Toast.makeText(MainActivity.this, getString(R.string.max_results), Toast.LENGTH_SHORT).show();
                        mShowResults.setText(String.valueOf(DEFAULT_RESULTS));
                        resultsRequested = DEFAULT_RESULTS;
                    }
                }
                queryUrl = queryToUrl(query, resultsRequested);

                // Initiates loader if there is an internet connection
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                if (activeNetwork != null && activeNetwork.isConnected()) {
                    mProgressIndicator.setVisibility(View.VISIBLE);
                    mEmptyStateTextView.setText("");
                    getLoaderManager().restartLoader(BOOK_LOADER_ID, null, MainActivity.this);
                } else {
                    mEmptyStateTextView.setText(R.string.no_internet);
                }
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        // Sets up clicks on the book items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the url for the book clicked
                Book currentEarthquake = mAdapter.getItem(position);
                Uri webpage = Uri.parse(currentEarthquake.getUrl());
                // Create and start intent
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });


    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, "on create loader: " + queryUrl);
        return new BookLoader(this, queryUrl);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {
        // If there is a valid list of {@link Book}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        Log.i(LOG_TAG, "ON LOAD FINISHED");
        if (books != null && !books.isEmpty()) {
            mAdapter.clear();
            mAdapter.addAll(books);
        }
        mProgressIndicator.setVisibility(View.GONE);
        mEmptyStateTextView.setText(R.string.empty);

        // Update the number of results available
        int numResults = QueryUtils.getTotalItems();
        mTotalResults.setText(getString(R.string.total_results, numResults));
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        Log.i(LOG_TAG, "ON LOADER RESET");
        mAdapter.clear();
    }

    private String queryToUrl (String queryText, int numberOfResults) {
        // Replaces the blanks to make a valid url
        String data = queryText.replace(" ", "+");
        return String.format(QUERY_FORMAT, data, numberOfResults);
    }
}
