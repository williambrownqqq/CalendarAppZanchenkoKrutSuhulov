package com.ZanchenkoKrutSugulov.calendarapp.database.dao

import android.util.Log
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.Calendar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CalendarDatabase : CalendarDao {
    private val database = FirebaseDatabase.getInstance()
    private val collection = database.getReference("calendars")


    override fun createCalendar(calendar: Calendar) {
        collection.child(calendar.calendarId).setValue(calendar)
    }

    override fun updateCalendar(calendar: Calendar) {
        collection.child(calendar.calendarId).setValue(calendar)
    }

    override fun deleteCalendar(calendarId: String) {
        collection.child(calendarId).removeValue()
    }

    override fun getCalendars(userId: String, callback: (List<Calendar>) -> Unit) {
        TODO("Not yet implemented")
    }

}
