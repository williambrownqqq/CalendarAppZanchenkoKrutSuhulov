package com.ZanchenkoKrutSugulov.calendarapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import android.app.Activity
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.auth.GoogleAuthProvider


@RequiresApi(Build.VERSION_CODES.O)
class UserProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonLogout: Button
    private lateinit var backButton: ImageView
    private lateinit var userEmailView: TextView
    private var currentUser: FirebaseUser? = null

    private lateinit var editUserEmail: EditText
    private lateinit var textPassword: TextView
    private lateinit var editPassword: EditText
    private lateinit var buttonConnectGoogle: Button

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        auth = FirebaseAuth.getInstance()
        buttonLogout = findViewById(R.id.logout)
        backButton = findViewById(R.id.backFromUserProfile)
        userEmailView = findViewById(R.id.userEmail)
        currentUser = auth.currentUser
        editUserEmail = findViewById(R.id.editUserEmail)
        textPassword = findViewById(R.id.textPassword)
        editPassword = findViewById(R.id.editPassword)
        buttonConnectGoogle = findViewById(R.id.buttonConnectGoogle)

        loadUserProfile()

        buttonLogout.setOnClickListener {
            auth.signOut()
            startLoginActivity()
        }

        backButton.setOnClickListener {
            finish()
        }


        checkGoogleAccountConnected()
        configureGoogleSignIn()


        buttonConnectGoogle.setOnClickListener {
            if (isGoogleAccountConnected()) {
                Toast.makeText(this, "Google account already connected.", Toast.LENGTH_SHORT).show()
            } else {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }
        }
    }
    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
    }
    private fun checkGoogleAccountConnected() {
        if (isGoogleAccountConnected()) {
            buttonConnectGoogle.text = "Not Connected"
        } else {
            buttonConnectGoogle.text = "Connected"
        }
    }

    private fun isGoogleAccountConnected(): Boolean {
        Log.d("UserProfileActivity", "!CHECK CURRENT USER: ${currentUser.toString()}")
        return currentUser?.providerId == GoogleAuthProvider.PROVIDER_ID
    }


    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                Toast.makeText(
                    this,
                    "Success! ${account.displayName.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
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
}