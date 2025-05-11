package com.example.foodreviewapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    // references to the database and UI elements
    private val dbRef = FirebaseDatabase.getInstance().reference
    private lateinit var autoComplete: AutoCompleteTextView
    private lateinit var adView : AdView
    private lateinit var addRestaurantBtn: Button
    private lateinit var setNameBtn : Button
    private lateinit var nameEt : EditText

    private lateinit var pref : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        pref = this.getSharedPreferences( this.packageName + "_preferences", Context.MODE_PRIVATE )
        editor = pref.edit()
        setNameBtn = findViewById(R.id.btn_set_name)
        nameEt = findViewById(R.id.et_name)
        nameEt.setText(pref.getString("REVIEWER_NAME", "Anonymous"))
        setNameBtn.setOnClickListener {
            setName()
        }
        // gets the view by ID
        autoComplete = findViewById(R.id.search_view)
        addRestaurantBtn = findViewById(R.id.btnGoToAddRestaurant)

        addRestaurantBtn.setOnClickListener {
            val intent = Intent(this, AddRestaurantActivity::class.java)
            startActivity(intent)
        }
        // real time event listener on database
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot : DataSnapshot) {
                // get a string list of all top level children, which are the restaurant names
                val restaurantNames : List<String> = snapshot.children.mapNotNull { it.key }
                // converts the list to an array adapter and populates the autocomplete element
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, restaurantNames)
                autoComplete.setAdapter(adapter)
            }

            override fun onCancelled(error : DatabaseError) {
                Log.w("MainActivity", "Failed to fetch restaurant names", error.toException())
            }
        })

        // listens for user selected an item in the autocomplete
        autoComplete.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position).toString()
            goToRestaurantView(selected)
        }

        // listens for the user typing and searching in the autocomplete
        autoComplete.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val typed = v.text.toString().trim()
                if (typed.isNotEmpty()) {
                    goToRestaurantView(typed)
                }
                true
            } else {
                false
            }
        }

        // creating banner ad
        adView = AdView(this)
        // default sizing and fake ad ID
        val adSize = AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT)
        val adUnitId : String = "ca-app-pub-3940256099942544/6300978111"
        adView.setAdSize(adSize)
        adView.adUnitId = adUnitId

        // ad builder with food-related topics
        val builder = AdRequest.Builder()
        builder.addKeyword("food")
        val request = builder.build()

        // sets view in activity_main.xml to display the ad and loads from google
        val adLayout : ConstraintLayout = findViewById(R.id.banner_ad)
        adLayout.addView(adView)
        adView.loadAd(request)
    }

    private fun setName() {
        val name : String = nameEt.text.toString()
        editor.putString("REVIEWER_NAME", name)
        editor.commit()
    }

    // function that creates intent and passes restaurant name to next activity
    private fun goToRestaurantView(name : String) {
        val intent = Intent(this, RestaurantActivity::class.java)
        intent.putExtra("NAME", name)
        startActivity(intent)
    }
}