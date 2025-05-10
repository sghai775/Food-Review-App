package com.example.foodreviewapp
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ProcessReview :  AppCompatActivity(){
    private lateinit var database: FirebaseDatabase
    private lateinit var restaurantRef: DatabaseReference
    private lateinit var ratingBar : RatingBar
    private lateinit var ratingValue : TextView
    private lateinit var submit : Button
    private lateinit var reviewerEdit : EditText
    private lateinit var reviewTextBox : EditText
    private lateinit var restaurantName : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.review)
        restaurantName = intent.getStringExtra("NAME") ?: ""
        database = FirebaseDatabase.getInstance()
        restaurantRef = database.getReference(restaurantName)
        ratingBar = findViewById(R.id.ratingBarInput)
        ratingValue = findViewById(R.id.tvRatingValue)
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            ratingValue.text = "$rating"
        }
        submit = findViewById(R.id.btnSubmitReview)
        submit.setOnClickListener {
            submitReview()
        }
    }

    private fun submitReview() {

        reviewerEdit =findViewById((R.id.etReviewerName))
        val reviewerName = reviewerEdit.text.toString().trim()
        val rating = ratingBar.rating.toDouble()
        reviewTextBox = findViewById(R.id.etReviewText)
        val reviewText = reviewTextBox.text.toString().trim()

        if (reviewerName.isEmpty()) {
            reviewerEdit.error = "Please enter your name"
            return
        }

        if (reviewText.isEmpty()) {
            reviewTextBox.error = "Please enter your review"
            return
        }
        val reviewData = hashMapOf(
            "Date" to "",
            "Rating" to rating,
            "Text" to reviewText
        )

        restaurantRef.child("Reviews").child(reviewerName).setValue(reviewData)
            .addOnSuccessListener {
                Log.d("Firebase", "All G!")
                updateRestaurantStats(rating)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "All B!", e)
            }
    }

    private fun updateRestaurantStats(newRating: Double) {
        restaurantRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentAvg = snapshot.child("AverageRating").getValue(Double::class.java) ?: 0.0
                val currentCount = snapshot.child("NumRating").getValue(Int::class.java) ?: 0

                val newCount = currentCount + 1
                val newAvg = ((currentAvg * currentCount) + newRating) / newCount

                restaurantRef.child("AverageRating").setValue(newAvg)
                restaurantRef.child("NumRating").setValue(newCount)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "ALL B!", error.toException())
            }
        })
    }
}