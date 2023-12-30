package com.ZanchenkoKrutSugulov.calendarapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ZanchenkoKrutSugulov.calendarapp.utils.createUserDB
import com.ZanchenkoKrutSugulov.calendarapp.utils.isValidEmail
import com.ZanchenkoKrutSugulov.calendarapp.utils.updateUserAfterGoogleRegister
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.textfield.TextInputEditText
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@RequiresApi(Build.VERSION_CODES.O)
class Register : AppCompatActivity() {
    private var editTextEmail: TextInputEditText? = null
    private var editTextPassword: TextInputEditText? = null
    private var buttonReg: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressBar? = null
    private var textView: TextView? = null

    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        buttonReg = findViewById(R.id.btn_register);
        textView = findViewById(R.id.loginNow);
        textView!!.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        })

        buttonReg!!.setOnClickListener OnClickListener@{
            progressBar!!.visibility = View.VISIBLE
            val email: String = editTextEmail!!.text.toString().trim()
            val password: String = editTextPassword!!.text.toString().trim()

            if (!isValidEmail(email)) {
                Toast.makeText(this@Register, "Invalid email address", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this@Register,"Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            Log.d("RegisterActivity", "FirebaseAuth instance: $mAuth")
            registerUserWithEmail(email, password)
        }

        configureGoogleSignIn()

        findViewById<ImageView>(R.id.loginWithGoogle).setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun registerUserWithEmail(email: String, password: String) {
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressBar!!.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = mAuth!!.currentUser
                    createUserDB(user)
                    Toast.makeText(this@Register, "Account created.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, Login::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this@Register,
                        task.exception?.message ?: "Authentication failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("RegisterActivity", "Authentication failed!", task.exception)
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

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

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
                    Toast.makeText(applicationContext, "Register with Google", Toast.LENGTH_SHORT)
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

    private fun startMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}