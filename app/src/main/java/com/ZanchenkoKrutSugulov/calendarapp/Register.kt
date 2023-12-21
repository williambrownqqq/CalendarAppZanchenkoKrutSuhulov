package com.ZanchenkoKrutSugulov.calendarapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Register : AppCompatActivity() {
    private var editTextEmail: TextInputEditText? = null
    private var editTextPassword: TextInputEditText? = null
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

    fun isValidEmail(email: String): Boolean {
        val regex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
        return regex.matches(email)
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
                Toast.makeText(
                    this@Register,
                    "Password must be at least 6 characters long",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }

            Log.d("RegisterActivity", "FirebaseAuth instance: $mAuth")

            mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressBar!!.visibility = View.GONE
                    if (task.isSuccessful) {
                        val user = mAuth!!.currentUser
                        val db = FirebaseFirestore.getInstance()

                        val newUser = User(
                            uid = user!!.uid,
                            email = user.email!!,
                            firstName = "Имя",
                            lastName = "Фамилия"
                        )

                        db.collection("users").document(user.uid).set(newUser)
                            .addOnSuccessListener {
                                Log.d("RegisterActivity", "Данные пользователя сохранены в Firestore")
                            }
                            .addOnFailureListener { e ->
                                Log.w("RegisterActivity", "Ошибка при сохранении данных пользователя", e)
                            }
                        Toast.makeText(
                            this@Register, "Account created.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(applicationContext, Login::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val exception = task.exception
                        val message = exception?.message ?: "Authentication failed!"
                        Toast.makeText(this@Register, message, Toast.LENGTH_SHORT).show()
                        Log.d("RegisterActivity", "Authentication failed!", task.exception)
                    }
                }
        }
    }
}