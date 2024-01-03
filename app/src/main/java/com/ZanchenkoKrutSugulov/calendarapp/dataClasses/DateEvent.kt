package com.ZanchenkoKrutSugulov.calendarapp.dataClasses


data class DateEvent(
    var id: Int = 0,
    var year: Int = 0,
    var month: Int = 0,
    var day: Int = 0,
    var hour: Int? = null,
    var minute: Int? = null,
    var name: String = "",
    var description: String? = null,
    var calendarId: String? = null
)
