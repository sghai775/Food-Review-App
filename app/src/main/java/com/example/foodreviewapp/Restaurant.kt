@file:Suppress("ConvertSecondaryConstructorToPrimary")

package com.example.foodreviewapp
import java.util.*
import java.text.SimpleDateFormat

class Restaurant {
    private var name : String = ""
    private var category : String = ""
    private var averageRating : Float = 0.0f
    private var totalRating : Float = 0.0f
    private var numRatings : Int = 0
    private lateinit var reviews : ArrayList<Review>

    constructor(name : String, category : String, totalRating : Float, numRatings : Int, averageRating : Float) {
        this.name = name
        this.category = category
        this.reviews = ArrayList()
        this.totalRating = totalRating
        this.numRatings = numRatings
        this.averageRating = averageRating
    }

    fun getName() : String {
        return this.name
    }

    fun getCategory() : String {
        return this.category
    }

    fun getReviews() : ArrayList<Review> {
        return this.reviews
    }

    fun getAverageRating() : Float {
        return this.averageRating
    }

    fun addReview(review : Review) {
        this.reviews.add(review)
    }

    fun sortReviewsByDate() {
        reviews.sortByDescending {  SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).parse(it.getDate()) ?: Date(0)}
    }

}