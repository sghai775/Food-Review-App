@file:Suppress("ConvertSecondaryConstructorToPrimary")

package com.example.foodreviewapp

class Restaurant {
    private var name : String = ""
    private var category : String = ""
    private var averageRating : Float = 0.0f
    private var totalRating : Float = 0.0f
    private var numRatings : Int = 0
    private lateinit var reviews : ArrayList<Review>

    constructor(name : String, category : String) {
        this.name = name
        this.category = category
        this.reviews = ArrayList()
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

    fun addReview(review : Review) {
        this.numRatings += 1
        this.totalRating += review.getRating()
        this.averageRating = this.totalRating / this.numRatings
        this.reviews.add(review)
    }

}