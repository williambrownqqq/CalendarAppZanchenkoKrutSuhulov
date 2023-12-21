package com.ZanchenkoKrutSugulov.calendarapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.User


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonLogout: Button
    private lateinit var textViewUserDetails: TextView
    private lateinit var textViewName: TextView
    private lateinit var textViewLastname: TextView
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        buttonLogout = findViewById(R.id.logout)
        textViewUserDetails = findViewById(R.id.user_details)
        textViewName = findViewById(R.id.user_name)
        textViewLastname = findViewById(R.id.user_surname)
        currentUser = auth.currentUser

        if (currentUser == null) {
            startLoginActivity()
        } else {
            loadUserProfile()
        }

        buttonLogout.setOnClickListener {
            auth.signOut()
            startLoginActivity()
        }
    }

    private fun startLoginActivity() {
//        val intent = Intent(this, Login::class.java)
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    private fun loadUserProfile() {
        val userId = currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userId)

        docRef.get().addOnSuccessListener { documentSnapshot ->
            val userProfile = documentSnapshot.toObject(User::class.java)
            textViewUserDetails.text = userProfile?.email
            textViewName.text = userProfile?.firstName ?: "No name"
            textViewLastname.text = userProfile?.lastName ?: "No surname"
        }.addOnFailureListener { exception ->
            Log.w("MainActivity", "Error getting user details: ", exception)
        }
    }
}