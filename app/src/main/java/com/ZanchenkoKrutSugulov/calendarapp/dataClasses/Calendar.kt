package com.ZanchenkoKrutSugulov.calendarapp.dataClasses

import java.io.Serializable

data class Calendar(
    val calendarId: String = "",
    val name: String = "",
    val userId: String = "",
    val primary: Boolean = false
) : Serializable
