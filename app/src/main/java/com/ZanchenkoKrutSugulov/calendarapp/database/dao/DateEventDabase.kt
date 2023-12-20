package com.ZanchenkoKrutSugulov.calendarapp.database.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db.DateEvent

@Database(entities=[DateEvent::class], version = 1, exportSchema = false)
abstract class DateEventDatabase: RoomDatabase() {
    abstract fun dateEventDao(): DateEventDao
    companion object {
        @Volatile
        private var INSTANCE: DateEventDatabase? = null
        fun getInstance(context: Context): DateEventDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null)
                    instance = Room.databaseBuilder(context.applicationContext, DateEventDatabase::class.java, "date_database").build()

                return instance
            }
        }
    }
}