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
import android.widget.Toast
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
    private lateinit var editEmailButton: Button
    private lateinit var textPassword: TextView
    private lateinit var editPassword: EditText
    private lateinit var editPasswordButton: Button
    private lateinit var saveEmailButton: Button
    private lateinit var savePasswordButton: Button

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
        saveEmailButton = findViewById(R.id.saveEmailButton)
        savePasswordButton = findViewById(R.id.savePasswordButton)

        saveEmailButton.setOnClickListener {
            val newEmail = editUserEmail.text.toString()
            updateEmail(newEmail)
        }

        savePasswordButton.setOnClickListener {
            val newPassword = editPassword.text.toString()
            updatePassword(newPassword)
        }

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

    private fun updateEmail(newEmail: String) {
        currentUser?.let { user ->
            user.verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener { verificationTask ->
                    if (verificationTask.isSuccessful) {
                        Log.d("UserProfileActivity", "Verification email sent to $newEmail")
                        Toast.makeText(this, "Check your email to verify the new address", Toast.LENGTH_LONG).show()

                        editUserEmail.visibility = View.GONE
                        userEmailView.visibility = View.VISIBLE
                    } else {
                        Log.w("UserProfileActivity", "Error sending verification email.", verificationTask.exception)
                        Toast.makeText(this, "Error sending verification email.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


    private fun updatePassword(newPassword: String) {
        currentUser?.let { user ->
            user.updatePassword(newPassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("UserProfileActivity", "Password updated.")
                    // Сбросьте отображение пароля
                    textPassword.text = newPassword.map { '*' }.joinToString("")
                    editPassword.visibility = View.GONE
                    textPassword.visibility = View.VISIBLE
                } else {
                    Log.w("UserProfileActivity", "Error updating password.", task.exception)
                    Toast.makeText(this, "Error updating password.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}