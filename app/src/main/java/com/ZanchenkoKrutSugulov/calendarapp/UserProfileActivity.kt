package com.ZanchenkoKrutSugulov.calendarapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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

    private lateinit var editUserEmail: EditText
    private lateinit var editEmailButton: ImageView
    private lateinit var textPassword: TextView
    private lateinit var editPassword: EditText
    private lateinit var editPasswordButton: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        auth = FirebaseAuth.getInstance()
        buttonLogout = findViewById(R.id.logout)
        backButton = findViewById(R.id.backFromUserProfile)
        userEmailView = findViewById(R.id.userEmail)
        currentUser = auth.currentUser
        editUserEmail = findViewById(R.id.editUserEmail)
        editEmailButton = findViewById(R.id.editEmailButton)
        textPassword = findViewById(R.id.textPassword)
        editPassword = findViewById(R.id.editPassword)
        editPasswordButton = findViewById(R.id.editPasswordButton)


        loadUserProfile()
        buttonLogout.setOnClickListener {
            auth.signOut()
            startLoginActivity()
        }

        backButton.setOnClickListener {
            finish()
        }


        editEmailButton.setOnClickListener {
            toggleEditing(userEmailView, editUserEmail)
        }

        editPasswordButton.setOnClickListener {
            toggleEditing(textPassword, editPassword)
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
            Log.w("UserProfileActivity", "Error getting user details: ", exception)
        }
    }

    private fun toggleEditing(textView: TextView, editText: EditText) {
        if (editText.visibility == View.VISIBLE) {
            textView.text = editText.text
            textView.visibility = View.VISIBLE
            editText.visibility = View.GONE
        } else {
            editText.setText(textView.text)
            editText.visibility = View.VISIBLE
            textView.visibility = View.GONE
        }
    }
}