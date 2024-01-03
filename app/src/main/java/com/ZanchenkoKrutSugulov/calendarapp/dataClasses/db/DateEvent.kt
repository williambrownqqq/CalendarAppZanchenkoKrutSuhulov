package com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db

data class DateEvent(
    var id: Int = "", // Use String as ID for Firebase
    var year: Int = 0,
    var month: Int = 0,
    var day: Int = 0,
    var hour: Int? = null,
    var minute: Int? = null,
    var name: String = "",
    var description: String? = "",
    var calendarId: Int? = 0
)
