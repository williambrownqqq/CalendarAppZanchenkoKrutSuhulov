package com.ZanchenkoKrutSugulov.calendarapp.database.dao

import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent

interface DateEventDao {
    fun insertDateEvent(dateEvent: DateEvent)
    fun updateDateEvent(eventId: String, updatedDateEvent: DateEvent)
    fun deleteDateEvent(eventId: String)
    fun getAllEvents(callback: (List<DateEvent>) -> Unit)
    fun getMonthEvents(year: Int, month: Int, callback: (List<DateEvent>) -> Unit)
    fun getDateEvent(eventId: String, callback: (DateEvent?) -> Unit)
    fun getDateEvents(
        year: Int,
        month: Int,
        day: Int,
        calendarId: String,
        callback: (List<DateEvent>) -> Unit
    )
}

//interface DateEventDao {
//    fun insertDateEvent(dateEvent: DateEvent)
//    fun updateDateEvent(eventId: String, updatedDateEvent: DateEvent)
//    fun deleteDateEvent(eventId: String)
//    fun getAllEvents(callback: (List<DateEvent>) -> Unit)
//    fun getMonthEvents(year: Int, month: Int, callback: (List<DateEvent>) -> Unit)
//    fun getDateEvent(eventId: String, callback: (DateEvent?) -> Unit)
//    fun getDateEvents(year: Int, month: Int, day: Int, callback: (List<DateEvent>) -> Unit)
//}

//interface DateEventDao {
//    @Insert
//    suspend fun insertDateEvent(dateEvent: DateEvent)
//
//    @Update
//    suspend fun updateDateEvent(dateEvent: DateEvent)
//
//    @Delete
//    suspend fun deleteDateEvent(dateEvent: DateEvent)
//
//    @Query("SELECT * FROM date_event_table")
//    fun getAllDateEvents(): LiveData<List<DateEvent>>
//
//    @Query("SELECT * FROM date_event_table WHERE event_year = :year AND event_month = :month ORDER BY event_day ASC")
//    fun getMonthEvents(year: Int, month: Int): LiveData<List<DateEvent>>
//
//    @Query("SELECT * FROM date_event_table WHERE event_day = :day AND event_month = :month AND event_year = :year ORDER BY event_day ASC, event_month ASC, event_year ASC")
//    fun getDateEvents(year: Int, month: Int, day: Int): LiveData<List<DateEvent>>
//
//    @Query("SELECT * FROM date_event_table WHERE event_id = :eventId")
//    fun getDateEvent(eventId: Int): LiveData<DateEvent>
//}