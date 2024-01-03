package com.ZanchenkoKrutSugulov.calendarapp.firebaseDB

import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.DateEventDao
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

object FirebaseRealTimeDatabase: DateEventDao {

    private val database = FirebaseDatabase.getInstance()
    private val reference = database.getReference("date_events")

    override suspend fun insertDateEvent(dateEvent: DateEvent) {
        val eventId = reference.push().key ?: ""
        reference.child(eventId).setValue(dateEvent)
    }

    override suspend fun updateDateEvent(eventId: String, updatedDateEvent: DateEvent) {
        // Update the DateEvent using its ID/key
        reference.child(eventId).setValue(updatedDateEvent)
    }

    override suspend fun deleteDateEvent(eventId: String) {
        // Remove the DateEvent using its ID/key
        reference.child(eventId).removeValue()
    }

    override suspend fun getAllDateEvents(listener: ValueEventListener) {
        // Attach a ValueEventListener to get all data
        reference.addListenerForSingleValueEvent(listener)
    }

    override suspend fun getDateEvent(eventId: String, listener: ValueEventListener) {
        // Attach a ValueEventListener to get specific data by ID
        reference.child(eventId).addListenerForSingleValueEvent(listener)
    }
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