package com.ZanchenkoKrutSugulov.calendarapp.utils

import android.util.Log
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

fun createUserDB(user: FirebaseUser?) {
    val db = FirebaseFirestore.getInstance()

    val newUser = User(
        uid = user!!.uid,
        email = user.email!!
    )

    db.collection("users").document(user.uid).set(newUser)
        .addOnSuccessListener {
            Log.d("RegisterActivity", "Дані користувача успішно збережені")
        }
        .addOnFailureListener { e ->
            Log.w("RegisterActivity", "Помилка збереження даних", e)
        }
}

fun isValidEmail(email: String): Boolean {
    val regex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    return regex.matches(email)
}