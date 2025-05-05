@file:Suppress("ConvertSecondaryConstructorToPrimary")

package com.example.foodreviewapp

class Review {
    private var reviewerName : String = ""
    /* Rating scale of 0-5*/
    private var rating : Int = 0
    private var reviewText : String = ""
    /* Tentatively storing date as string until we determine what's returned or processed by Date
     * Picker or Calendar GUI
     */
    private var date : String = ""
    /* If we plan to use photos, we can store the raw photo in Firebase storage and the respective
     * url into Firebase Realtime. There's a library called Glide to fetch and load the image into
     * an ImageView, so we could possibly use that instead of having to use a bitmap, but I'm not
     * sure we can include other dependencies
     */
    private var url : String = ""

    constructor(reviewerName : String, rating : Int, reviewText : String, date : String, url : String) {
        this.reviewerName = reviewerName
        this.rating = rating
        this.reviewText = reviewText
        this.date = date
        this.url = url
    }

    fun getName() : String {
        return this.reviewerName
    }

    fun getRating() : Int {
        return this.rating
    }

    fun getDate() : String {
        return this.date
    }

    fun getURL() : String {
        return this.url
    }
}