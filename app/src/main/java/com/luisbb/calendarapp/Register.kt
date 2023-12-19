package com.luisbb.calendarapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth


class Register: AppCompatActivity() {
    private var editTextEmail: TextInputEditText? = null
    private var editTextPassword:TextInputEditText? = null
    private var buttonReg: Button? = null
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

        buttonReg!!.setOnClickListener(View.OnClickListener {
            progressBar!!.visibility = View.VISIBLE
            val email: String = editTextEmail!!.text.toString()
            val password: String = editTextPassword!!.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@Register, "Enter email", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@Register, "Enter password", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressBar!!.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@Register, "Account created.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(applicationContext, Login::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            this@Register, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        })
    }
}