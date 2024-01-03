package com.ZanchenkoKrutSugulov.calendarapp.database.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.DateEventDao
import com.ZanchenkoKrutSugulov.calendarapp.firebaseDB.FirebaseRealTimeDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
object DateEventDatabase {

    private val database = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dateEventReference: DatabaseReference = database.reference.child("date_events").child(auth.currentUser?.uid ?: "")

    fun dateEventDao(): DateEventDao {
        return FirebaseRealTimeDatabase
    }
        @Volatile
        private var INSTANCE: DateEventDatabase? = null

        fun getInstance(): DateEventDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = DateEventDatabase
                INSTANCE = instance
                instance
            }
        }
    }