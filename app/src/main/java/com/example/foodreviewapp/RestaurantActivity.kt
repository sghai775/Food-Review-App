package com.example.foodreviewapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RestaurantActivity : AppCompatActivity() {
    private lateinit var restaurantName : TextView;
    private lateinit var typeOfFood : TextView;
    private lateinit var rating : RatingBar;
    private lateinit var averageRating : TextView;
    private lateinit var restaurant : Restaurant;
    private lateinit var recyclerView: RecyclerView;
    private lateinit var reviewsAdapter: ReviewsAdapter
    private lateinit var reviewButton : FloatingActionButton
    private lateinit var name : String
    private lateinit var backButton : FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.restaurant_view)
        restaurantName = findViewById(R.id.restaurant_name)
        typeOfFood = findViewById(R.id.food_type)
        rating = findViewById(R.id.average_rating_bar)
        averageRating = findViewById(R.id.average_rating_text)
        recyclerView = findViewById((R.id.reviews_recycler_view))


        recyclerView.layoutManager = LinearLayoutManager(this)
        reviewsAdapter = ReviewsAdapter(emptyList())
        recyclerView.adapter = reviewsAdapter
        name = intent.getStringExtra("NAME") ?: ""

        fetchFirebaseData()

        reviewButton = findViewById(R.id.leaveReview)
        reviewButton.setOnClickListener {

            val intent : Intent = Intent(this, ProcessReview::class.java)
            intent.putExtra("NAME", name)
            startActivity(intent)
        }
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener{
            finish()


        }

    }

    private fun fetchFirebaseData() {
        val restaurantRef = FirebaseDatabase.getInstance().getReference(name)
        restaurantRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val avgRating = snapshot.child("AverageRating").getValue(Float::class.java) ?: 0f
                val category = snapshot.child("Category").getValue(String::class.java) ?: ""
                val numRating = snapshot.child("NumRating").getValue(Int::class.java) ?: 0
                val totalRating = snapshot.child("TotalRating").getValue(Float::class.java) ?: 0f

                restaurant = Restaurant(name, category, totalRating, numRating, avgRating);

                val reviewsSnapshot = snapshot.child("Reviews")

                if (reviewsSnapshot.exists() && reviewsSnapshot.value != "") {
                    for (reviewSnapshot in reviewsSnapshot.children) {
                        val rName : String = reviewSnapshot.key ?: "Anonymous"
                        val review = Review(
                            reviewerName = rName.filter { char ->
                                char.isLetter() || char == ' ' || char == '.' || char == '\'' ||  char == ',' },
                            rating = reviewSnapshot.child("Rating").getValue(Int::class.java) ?: 0,
                            reviewText = reviewSnapshot.child("Text").getValue(String::class.java) ?: "",
                            date = reviewSnapshot.child("Date").getValue(String::class.java) ?: "",
                            url = ""
                        )
                        restaurant.addReview(review)
                    }
                }

                restaurant.sortReviewsByDate()
                reviewsAdapter = ReviewsAdapter(restaurant.getReviews())
                recyclerView.adapter = reviewsAdapter

                restaurantName.text = restaurant.getName()
                val categoryText : String = "Type of Food: ${restaurant.getCategory()}"
                typeOfFood.text = categoryText
                val ratingText : String = "Rating: %.2f".format(restaurant.getAverageRating())
                averageRating.text = ratingText
                rating.rating = restaurant.getAverageRating()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read data: ${error.message}")
            }
        })
    }

    inner class ReviewsAdapter(private val reviews: List<Review>) :
        RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

        inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val reviewerName: TextView = itemView.findViewById(R.id.reviewer_name)
            val ratingBar: RatingBar = itemView.findViewById(R.id.review_rating_bar)
            val reviewText: TextView = itemView.findViewById(R.id.review_text)
            val reviewDate: TextView = itemView.findViewById(R.id.review_date)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.review_panel, parent, false)
            return ReviewViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
            val review = reviews[position]
            holder.reviewerName.text = review.getName()
            holder.ratingBar.rating = review.getRating().toFloat()
            holder.reviewText.text = review.getReviewText()
            holder.reviewDate.text = review.getDate()
        }

        override fun getItemCount() = reviews.size
    }
}