package com.ZanchenkoKrutSugulov.calendarapp.dataClasses


data class DateEvent(
    var id: String,
    var year: Int,
    var month: Int,
    var day: Int,
    var hour: Int?,
    var minute: Int?,
    var name: String,
    var description: String?,
    var calendarId: String?
)
