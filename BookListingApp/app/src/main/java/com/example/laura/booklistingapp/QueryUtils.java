package com.example.laura.booklistingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

/**
 * Created by Laura on 6/6/17.
 */

public class QueryUtils {

    private static final String LOG_TAG = "QueryUtils";
    private static int totalItems = 0;

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the Google Books dataset and return a {@link Book} object to represent a single book.
     */
    public static List<Book> fetchBookData(Context context, String requestUrl) {
        Log.i(LOG_TAG, "FETCH DATA");

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Earthquake} object
        List<Book> books = extractBooks(context, jsonResponse);
        books = extractImages(context, books);

        // Return the {@link Event}
        return books;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
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

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
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

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing a JSON response.
     */
    public static List<Book> extractBooks(Context context, String jsonResponse) {

        // Create an empty ArrayList that we can start adding earthquakes to
        List<Book> books = new ArrayList<>();

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Convert SAMPLE_JSON_RESPONSE String into a JSONObject
            JSONObject rootJson = new JSONObject(jsonResponse);
            totalItems = rootJson.getInt("totalItems");

            if (totalItems == 0) {
                return null;
            }
            JSONArray items = rootJson.getJSONArray("items");

            JSONObject item, volumeInfo, imageLinks;
            JSONArray authors;
            String title, imageUrl, url;
            String author = "";
            double rating;
            for (int i = 0; i < items.length(); i++) {
                // Select book item
                item = items.getJSONObject(i);
                volumeInfo = item.getJSONObject("volumeInfo");

                title = volumeInfo.getString("title");

                try {
                    authors = volumeInfo.getJSONArray("authors");
                    for (int j = 0; j < authors.length(); j++) {
                        if (j == 0) {
                            author = authors.getString(j);
                        } else {
                            author = author + ", " + authors.getString(j);
                        }
                    }
                } catch (JSONException e) {
                    author = context.getString(R.string.no_author);
                }

                try {
                    imageLinks = volumeInfo.getJSONObject("imageLinks");
                    imageUrl = imageLinks.getString("smallThumbnail");
                } catch (JSONException e) {
                    imageUrl = null;
                }

                url = volumeInfo.getString("canonicalVolumeLink");

                try {
                    rating = volumeInfo.getDouble("averageRating");
                } catch (JSONException e) {
                    rating = 0;
                }
                // Create Book java object from data
                // Add book to list of books
                books.add(new Book(title, author, null, rating, url, imageUrl));
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
            Log.v(LOG_TAG, jsonResponse);
        }

        // Return the list of books
        return books;
    }

    private static List<Book> extractImages (Context context, List<Book> books) {

        if (books == null) {
            return null;
        }

        String imageUrl;
        Book currentBook;
        List<Book> booksWithImages = new ArrayList<Book>();

        for (int i = 0; i < books.size(); i++) {
            currentBook = books.get(i);
            imageUrl = currentBook.getImageUrl();
            Bitmap bmp = null;

            if (imageUrl == null) {
                bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.blank_image);
                Log.i(LOG_TAG, "No image: " + currentBook.getTitle());
            } else {
                try {
                    URL ulr = new URL(imageUrl);
                    HttpURLConnection con = (HttpURLConnection) ulr.openConnection();
                    InputStream is = con.getInputStream();
                    bmp = BitmapFactory.decodeStream(is);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "No image");
                }
            }

            currentBook.setImageBmp(bmp);
            booksWithImages.add(currentBook);
        }

        return booksWithImages;
    }

    public static int getTotalItems() {
        return totalItems;
    }

}
