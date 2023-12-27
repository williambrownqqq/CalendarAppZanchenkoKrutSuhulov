package com.ZanchenkoKrutSugulov.calendarapp.firebaseDB

import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db.DateEvent
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseRealTimeDatabase {

    private val database = FirebaseDatabase.getInstance()
    private val reference = database.getReference("date_events")

    fun saveDateEventToFirebase(dateEvent: DateEvent) {
        val eventId = reference.push().key ?: ""
        reference.child(eventId).setValue(dateEvent)
    }

    fun updateDateEventInFirebase(eventId: String, updatedDateEvent: DateEvent) {
        // Update the DateEvent using its ID/key
        reference.child(eventId).setValue(updatedDateEvent)
    }

    fun deleteDateEventFromFirebase(eventId: String) {
        // Remove the DateEvent using its ID/key
        reference.child(eventId).removeValue()
    }

    // Get all DateEvents
    fun getAllDateEventsFromFirebase(listener: ValueEventListener) {

        // Attach a ValueEventListener to get the data
        reference.addListenerForSingleValueEvent(listener)
    }

    // Get a specific DateEvent by ID
    fun getDateEventFromFirebase(eventId: String, listener: ValueEventListener) {

        // Attach a ValueEventListener to get the data
        reference.addListenerForSingleValueEvent(listener)
    }
}