package com.example.laura.booklistingapp;

import android.graphics.Bitmap;

/**
 * Created by Laura on 6/6/17.
 */

public class Book {

    String mTitle;
    String mAuthor;
    Bitmap mImageBmp;
    double mRating;
    String mUrl;
    String mImageUrl;

    public Book (String title, String author, Bitmap imageBmp, double rating, String url, String imageUrl) {
        mTitle = title;
        mAuthor = author;
        mImageBmp = imageBmp;
        mRating = rating;
        mUrl = url;
        mImageUrl = imageUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public Bitmap getImageBmp() {
        return mImageBmp;
    }

    public void setImageBmp(Bitmap bmp) {
        mImageBmp = bmp;
    }

    public double getRating() {
        return mRating;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getImageUrl() {
        return mImageUrl;
    }
}
