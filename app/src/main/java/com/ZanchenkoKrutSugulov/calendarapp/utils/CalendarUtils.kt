package com.ZanchenkoKrutSugulov.calendarapp.utils

import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.Calendar
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.CalendarDatabase
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

fun createPrimaryCalendarForNewUser(userId: String) {
    val newCalendar = Calendar(
        calendarId = UUID.randomUUID().toString(),
        name = "Main",
        userId = userId,
        isPrimary = true
    )
    CalendarDatabase.createCalendar(newCalendar)
}
