package com.ZanchenkoKrutSugulov.calendarapp.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db.DateEvent
import com.google.firebase.database.ValueEventListener

interface DateEventDao {
    suspend fun saveDateEvent(dateEvent: DateEvent)
    suspend fun updateDateEvent(eventId: String, updatedDateEvent: DateEvent)
    suspend fun deleteDateEvent(eventId: String)
    suspend fun getAllDateEvents(listener: ValueEventListener)
    suspend fun getDateEvent(eventId: String, listener: ValueEventListener)
}