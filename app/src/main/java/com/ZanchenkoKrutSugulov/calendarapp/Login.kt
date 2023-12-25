package com.ZanchenkoKrutSugulov.calendarapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
class Login : AppCompatActivity() {
    private var editTextEmail: TextInputEditText? = null
    private var editTextPassword: TextInputEditText? = null
    private var buttonLogin: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressBar? = null
    private var textView: TextView? = null


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
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        editTextEmail = findViewById(R.id.email);
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
    }
}