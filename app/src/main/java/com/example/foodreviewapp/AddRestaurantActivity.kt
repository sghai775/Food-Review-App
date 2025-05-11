package com.example.foodreviewapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddRestaurantActivity : AppCompatActivity() {
    // Firebase Database reference
    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().reference

    private lateinit var nameInput: EditText
    private lateinit var typeInput: EditText
    private lateinit var submitBtn: Button
    private lateinit var cancelBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Inflate the layout
        setContentView(R.layout.add_restaurant)

        // Bind views
        nameInput = findViewById(R.id.etRestaurantNameInput)
        typeInput = findViewById(R.id.etFoodTypeInput)
        submitBtn = findViewById(R.id.btnSubmitRestaurant)
        cancelBtn = findViewById(R.id.btnCancelRestaurant)

        // Set listeners
        submitBtn.setOnClickListener { addRestaurant() }
        cancelBtn.setOnClickListener { finish() }
    }

    private fun addRestaurant() {
        val name = nameInput.text.toString().trim()
        val type = typeInput.text.toString().trim()

        if (name.isEmpty()) {
            nameInput.error = "Please enter a restaurant name"
            return
        }
        if (type.isEmpty()) {
            typeInput.error = "Please enter a type of food"
            return
        }

        // Prepare data
        val restaurantData = mapOf(
            "Category" to type,
            "AverageRating" to 0.0,
            "NumRating" to 0,
            "Review" to {}
        )

        // Save to Firebase under Restaurants/{name}
        dbRef.child(name)
            .setValue(restaurantData)
            .addOnSuccessListener {
                Log.d("Firebase", "Restaurant '$name' added successfully")
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to add restaurant", e)
            }
    }
}

