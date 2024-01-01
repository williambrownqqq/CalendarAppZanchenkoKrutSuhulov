package com.ZanchenkoKrutSugulov.calendarapp.utils

import android.util.Log
import android.widget.Toast
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.User
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.CalendarDatabase
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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



fun updateUserAfterGoogleRegister(firebaseUser: FirebaseUser?, account: GoogleSignInAccount?) {
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


    CalendarDatabase.getUserPrimaryCalendar(firebaseUser.uid) { hasPrimary ->
        if (!hasPrimary) {
            Log.d("UserUtils", "!hasPrimary: ${hasPrimary}")
            createPrimaryCalendarForNewUser(firebaseUser.uid)
        }
    }
}
