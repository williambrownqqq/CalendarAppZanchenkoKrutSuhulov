package com.ZanchenkoKrutSugulov.calendarapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class UserProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonLogout: Button
    private lateinit var backButton: ImageView
    private lateinit var userEmailView: TextView
    private var currentUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        auth = FirebaseAuth.getInstance()
        buttonLogout = findViewById(R.id.logout)
        backButton = findViewById(R.id.backFromUserProfile)
        userEmailView = findViewById(R.id.userEmail)
        currentUser = auth.currentUser

        loadUserProfile()
        buttonLogout.setOnClickListener {
            auth.signOut()
            startLoginActivity()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun startLoginActivity() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    private fun loadUserProfile() {
        val userId = currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userId)

        docRef.get().addOnSuccessListener { documentSnapshot ->
            val userProfile = documentSnapshot.toObject(User::class.java)
            userEmailView.text = userProfile?.email
        }.addOnFailureListener { exception ->
            Log.w("MainActivity", "Error getting user details: ", exception)
        }
    }
}