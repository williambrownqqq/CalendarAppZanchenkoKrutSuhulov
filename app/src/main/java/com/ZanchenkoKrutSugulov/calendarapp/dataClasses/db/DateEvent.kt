package com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db


data class DateEvent(
    var id: Int,
    var year: Int,
    var month: Int,
    var day: Int,
    var hour: Int?,
    var minute: Int?,
    var name: String,
    var description: String?

)
