package com.ZanchenkoKrutSugulov.calendarapp.database.dao

import android.util.Log
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.Calendar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

object CalendarDatabase : CalendarDao {
    private val database = FirebaseDatabase.getInstance()
    private val collection = database.getReference("calendars")
    private val collectionEvents = database.getReference("date_events")

    override fun createCalendar(calendar: Calendar) {
        collection.child(calendar.calendarId).setValue(calendar)
    }

    override fun updateCalendar(calendar: Calendar) {
        collection.child(calendar.calendarId).setValue(calendar)
    }

    override fun deleteCalendar(calendarId: String) {
        collection.child(calendarId).removeValue()
        deleteAllCalendarEvents(calendarId)
    }

    override fun getCalendars(userId: String, callback: (List<Calendar>) -> Unit) {
        collection.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        callback(snapshot.children.mapNotNull { it.getValue(Calendar::class.java) })
                    } catch (e: Exception) {
                        Log.e("FirebaseCalendarDao", "Error parsing calendars", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseCalendarDao", "Error fetching calendars: ${error.message}")
                }
            })
    }

    private fun deleteAllCalendarEvents(calendarId: String) {
        collectionEvents.orderByChild("calendarId").equalTo(calendarId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { it.ref.removeValue() }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseCalendarDao", "Error deleting events: ${error.message}")
                }
            })
    }

    fun getUserPrimaryCalendar(userId: String, callback: (Boolean) -> Unit) {
        collection.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val hasPrimary = snapshot.children.any {
                        Log.d("CalendarDataBase", "!hasPrimary VALUE: ${it.value}")
                        Log.d("CalendarDataBase", "!hasPrimary VALUE: ${it.getValue(Calendar::class.java)}")
                        it.getValue(Calendar::class.java)?.primary == true
                    }
                    Log.d("CalendarDataBase", "!hasPrimary: $hasPrimary")

                    callback(hasPrimary)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseCalendarDao", "Error checking primary calendar: ${error.message}")
                    callback(false)
                }
            })
    }

}
