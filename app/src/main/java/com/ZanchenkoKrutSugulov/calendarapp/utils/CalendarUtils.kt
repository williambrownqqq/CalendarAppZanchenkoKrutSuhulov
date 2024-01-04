package com.ZanchenkoKrutSugulov.calendarapp.utils

import android.util.Log
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.Calendar
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.CalendarDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

fun createPrimaryCalendarForNewUser(userId: String) {
    val newCalendar = Calendar(
        calendarId = UUID.randomUUID().toString(),
        name = "Main",
        userId = userId,
        primary = true
    )
    CalendarDatabase.createCalendar(newCalendar)
}

fun getPrimaryCalendarForUser(userId: String, callback: (Calendar?) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val calendarRef = database.getReference("calendars")

    val query = calendarRef.orderByChild("userId").equalTo(userId)
    query.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val calendar = dataSnapshot.children
                .mapNotNull { it.getValue(Calendar::class.java) }
                .firstOrNull { it.primary }

            callback(calendar)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("FirebaseCalendarDao", "Error fetching calendar: ${databaseError.message}")
            callback(null)
        }
    })
}
