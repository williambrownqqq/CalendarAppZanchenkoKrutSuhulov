package com.ZanchenkoKrutSugulov.calendarapp.database.dao

import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.Calendar;

interface CalendarDao {
    fun createCalendar(calendar: Calendar)

    fun updateCalendar(calendar: Calendar)

    fun deleteCalendar(calendarId: String)

    fun getCalendars(userId:String, callback:(List<Calendar>) -> Unit)
}
