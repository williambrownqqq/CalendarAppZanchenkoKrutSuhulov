package com.ZanchenkoKrutSugulov.calendarapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes



@RequiresApi(Build.VERSION_CODES.O)
class Login : AppCompatActivity() {
    private var editTextEmail: TextInputEditText? = null
    private var editTextPassword: TextInputEditText? = null
    private var buttonLogin: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressBar? = null
    private var textView: TextView? = null

    lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var database: DatabaseReference

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonLogin = findViewById(R.id.btn_login)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.registerNow)
        textView!!.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, Register::class.java)
            startActivity(intent)
            finish()
        })
        buttonLogin!!.setOnClickListener(View.OnClickListener {
            progressBar!!.visibility = View.VISIBLE
            val email: String = editTextEmail!!.text.toString()
            val password: String = editTextPassword!!.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@Login, "Enter email", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@Login, "Enter password", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressBar!!.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@Login, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        })

        configureGoogleSignIn()

        findViewById<ImageView>(R.id.loginWithGoogle).setOnClickListener {
            signInWithGoogle()
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

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @SuppressLint("SuspiciousIndentation")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "#ERROR onActivityResult", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "firebaseAuthWithGoogle", Toast.LENGTH_SHORT)
                        .show()
                    startMainActivity()
                    updateUserAfterGoogleRegister(task.result?.user, account)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "#ERROR firebaseAuthWithGoogle",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateUserAfterGoogleRegister(firebaseUser: FirebaseUser?, account: GoogleSignInAccount?) {
        val db = FirebaseFirestore.getInstance()
        val user = db.collection("users").document(firebaseUser!!.uid)

        val userUpdates = hashMapOf<String, Any>(
            "googleAccountId" to (account?.id ?: ""),
            "email" to (account?.email ?: "")
        )

        user.update(userUpdates as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("UserProfileActivity", "User updated in db")
            }
            .addOnFailureListener { e ->
                Log.w("UserProfileActivity", "Error updating user in db! ", e)
            }
    }

    private fun createUserInDatabase(firebaseUser: FirebaseUser) {
        val user = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email
            // Добавьте другие поля, если требуется
        )

        database.child(firebaseUser.uid).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "User created in database", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Failed to create user in database: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}