package com.ZanchenkoKrutSugulov.calendarapp.database.dao

import android.util.Log
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.Query

object EventDatabase : DateEventDao {

    private val database = FirebaseDatabase.getInstance()
    private val collection = database.getReference("date_events")
    override fun insertDateEvent(dateEvent: DateEvent) {
        collection.child(collection.push().key ?: return).setValue(dateEvent)
    }

    override fun updateDateEvent(eventId: String, updatedDateEvent: DateEvent) {
        collection.child(eventId).setValue(updatedDateEvent)
    }

    override fun deleteDateEvent(eventId: String) {
        collection.child(eventId).removeValue()
    }

    override fun getAllEvents(callback: (List<DateEvent>) -> Unit) {
        queryEvents(collection, callback)
    }

    override fun getMonthEvents(year: Int, month: Int, callback: (List<DateEvent>) -> Unit) {
        collection.orderByChild("year").equalTo(year.toDouble())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(
                        "",
                        "getMonthEvents firebase: ${snapshot.children.mapNotNull { it.value }}"
                    )
                    val events = snapshot.children.mapNotNull { it.getValue(DateEvent::class.java) }
//                .filter { it.month == month }
                    callback(events)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseDateEventDao", "Error fetching events: ${error.message}")
                }
            })
    }


    //    override fun getDateEvents(
//        year: Int,
//        month: Int,
//        day: Int,
//        callback: (List<DateEvent>) -> Unit
//    ) {
//        Log.d("EventDatabase", "!Create Event getDateEvents")
//        val query = collection.orderByChild("event_year").equalTo(year.toDouble())
//            .orderByChild("event_month").equalTo(month.toDouble())
//            .orderByChild("event_day").equalTo(day.toDouble())
//        queryEvents(query, callback)
//    }
    override fun getDateEvents(
        year: Int,
        month: Int,
        day: Int,
        callback: (List<DateEvent>) -> Unit
    ) {
        Log.d("getDayEvents",  "!Create Event calendarDayClick Database")
        val query = collection.orderByChild("year").equalTo(year.toDouble())
        Log.d("getDayEvents",  "!Create Event calendarDayClick Database query $query")

        queryEvents(query) { events ->
            val filteredEvents = events.filter { it.month == month && it.day == day }
            callback(filteredEvents)
        }
    }

    override fun getDateEvent(eventId: String, callback: (DateEvent?) -> Unit) {
        collection.child(eventId).get().addOnSuccessListener {
            callback(it.getValue(DateEvent::class.java))
        }.addOnFailureListener {
            callback(null)
        }
    }

    private fun queryEvents(query: Query, callback: (List<DateEvent>) -> Unit) {
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = snapshot.children.mapNotNull { it.getValue(DateEvent::class.java) }
                Log.d("getMonthEvents", "!getMonthEvents: ${events.map { it }}")

                callback(events)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDateEventDao", "Error fetching events: ${error.message}")
            }
        })
    }
}
