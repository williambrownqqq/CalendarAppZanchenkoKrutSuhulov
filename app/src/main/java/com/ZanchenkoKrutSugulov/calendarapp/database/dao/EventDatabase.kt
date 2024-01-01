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
        val query = collection.orderByChild("event_year").equalTo(year.toDouble())
            .orderByChild("event_month").equalTo(month.toDouble())
        queryEvents(query, callback)
    }

    override fun getDateEvents(
        year: Int,
        month: Int,
        day: Int,
        callback: (List<DateEvent>) -> Unit
    ) {
        val query = collection.orderByChild("event_year").equalTo(year.toDouble())
            .orderByChild("event_month").equalTo(month.toDouble())
            .orderByChild("event_day").equalTo(day.toDouble())
        queryEvents(query, callback)
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
                callback(events)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDateEventDao", "Error fetching events: ${error.message}")
            }
        })
    }
}
//    fun saveDateEventToFirebase(dateEvent: DateEvent) {
//        val eventId = reference.push().key ?: ""
//        reference.child(eventId).setValue(dateEvent)
//    }
//
//    fun updateDateEventInFirebase(eventId: String, updatedDateEvent: DateEvent) {
//        // Update the DateEvent using its ID/key
//        reference.child(eventId).setValue(updatedDateEvent)
//    }
//
//    fun deleteDateEventFromFirebase(eventId: String) {
//        // Remove the DateEvent using its ID/key
//        reference.child(eventId).removeValue()
//    }
//
//    // Get all DateEvents
//    fun getAllDateEventsFromFirebase(listener: ValueEventListener) {
//
//        // Attach a ValueEventListener to get the data
//        reference.addListenerForSingleValueEvent(listener)
//    }
//
//    // Get a specific DateEvent by ID
//    fun getDateEventFromFirebase(eventId: String, listener: ValueEventListener) {
//
//        // Attach a ValueEventListener to get the data
//        reference.addListenerForSingleValueEvent(listener)
//    }
//}