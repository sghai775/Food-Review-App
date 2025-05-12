package com.example.foodreviewapp
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
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
import java.util.*
import java.text.SimpleDateFormat
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.ImageButton
import android.Manifest
import android.speech.RecognitionListener

class ProcessReview :  AppCompatActivity(){
    private lateinit var database: FirebaseDatabase
    private lateinit var restaurantRef: DatabaseReference
    private lateinit var ratingBar : RatingBar
    private lateinit var ratingValue : TextView
    private lateinit var submit : Button
    private lateinit var reviewerEdit : EditText
    private lateinit var reviewTextBox : EditText
    private lateinit var restaurantName : String
    private lateinit var pref : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private lateinit var btnSpeech : ImageButton
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var cancel : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.review)

        pref = this.getSharedPreferences( this.packageName + "_preferences", Context.MODE_PRIVATE )
        editor = pref.edit()
        reviewerEdit =findViewById((R.id.etReviewerName))
        reviewerEdit.setText(pref.getString("REVIEWER_NAME", "Anonymous"))
        restaurantName = intent.getStringExtra("NAME") ?: ""
        database = FirebaseDatabase.getInstance()
        restaurantRef = database.getReference(restaurantName)
        ratingBar = findViewById(R.id.ratingBarInput)
        ratingValue = findViewById(R.id.tvRatingValue)
        reviewTextBox = findViewById(R.id.etReviewText)
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            ratingValue.text = "$rating"
        }
        submit = findViewById(R.id.btnSubmitReview)
        submit.setOnClickListener {
            submitReview()
        }
        cancel = findViewById(R.id.btnCancelReview)
        cancel.setOnClickListener{
            finish()
        }

        btnSpeech = findViewById(R.id.btnMic)
        setupSpeechRecognition()
        setupPermissionHandler()
    }

    private fun submitReview() {
        val int = pref.getInt("REVIEW_NUMBER", 1)
        var reviewerName = reviewerEdit.text.toString().trim()
        reviewerName += int
        editor.putInt("REVIEW_NUMBER", int + 1)
        editor.commit()
        val rating = ratingBar.rating.toDouble()

        val reviewText = reviewTextBox.text.toString().trim()

        if (reviewerName.isEmpty()) {
            reviewerEdit.error = "Please enter your name"
            return
        }

        if (reviewText.isEmpty()) {
            reviewTextBox.error = "Please enter your review"
            return
        }

        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        val date : String = dateFormat.format(cal.time)

        val reviewData = hashMapOf(
            "Date" to date,
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
                restaurantRef.child("TotalRating").setValue((snapshot.child("TotalRating").getValue(Float::class.java) ?: 0f) + newRating)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "ALL B!", error.toException())
            }
        })
    }


    private fun setupPermissionHandler() {
        val permissionContract = ActivityResultContracts.RequestPermission()
        val permissionCallback = object : ActivityResultCallback<Boolean> {
            override fun onActivityResult(result : Boolean) {
                if (result) {
                    startSpeechRecognition()
                } else {
                    Toast.makeText(
                        this@ProcessReview,
                        "Speech recognition requires microphone permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        permissionLauncher = registerForActivityResult(permissionContract, permissionCallback)
    }

    private fun setupSpeechRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}

            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val currentText = reviewTextBox.text.toString()
                    val newText = if (currentText.isEmpty()) matches[0] else "$currentText ${matches[0]}"
                    reviewTextBox.setText(newText)
                }
            }

            override fun onPartialResults(partialResults: Bundle) {}
            override fun onEvent(eventType: Int, params: Bundle) {}
        })

        btnSpeech.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startSpeechRecognition()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your review")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        speechRecognizer.startListening(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}