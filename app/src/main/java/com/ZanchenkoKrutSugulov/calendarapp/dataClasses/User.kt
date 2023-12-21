package com.ZanchenkoKrutSugulov.calendarapp.dataClasses

data class User(
    val uid: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val googleAccountId: String? = null,
    val appleAccountId: String? = null,
    val outlookAccountId: String? = null
)
