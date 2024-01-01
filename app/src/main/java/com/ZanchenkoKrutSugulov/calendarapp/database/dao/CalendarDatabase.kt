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
        Log.d("Calendar Database", "!LOAD CALENDARS - getCalendars")
        Log.d("Calendar Database", "!LOAD CALENDARS - getCalendars - collection: $collection")
        collection.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("FirebaseCalendarDao", "Data snapshot: $snapshot")
                    try {
                        Log.d("Calendar Database", "!LOAD CALENDARS - getCalendars - calendars count: ${snapshot.children.count()}")
                        val items = mutableListOf<Calendar>()
                        for (i in snapshot.children) {
                            val item = i.value
                            Log.d("Calendar Database", "!LOAD CALENDARS - getCalendars - item: $item")
//                            item?.let { items.add(Calendar()) }
                        }
                        Log.d("Calendar Database", "!LOAD CALENDARS - getCalendars - items: $items")



                        val calendars = snapshot.children.mapNotNull { it.getValue(Calendar::class.java) }
                        Log.d("Calendar Database", "!LOAD CALENDARS - getCalendars - calendars: $calendars")


                        callback(calendars)
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
}
