package com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.EventDatabase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
class CreateEventViewModel(private val application: Application, val date: ZonedDateTime): ViewModel() {

    var eventName = ""
    var eventDescription = ""
    var calendarId = ""
    var id: String? = null

    var day = date.dayOfMonth
    var month = date.monthValue
    var year = date.year

    var hour: Int? = null
    var minute: Int? = null

    private fun createThisDateEvent(): DateEvent {
        return DateEvent(
            id ?: "",
            year,
            month,
            day,
            hour,
            minute,
            eventName,
            eventDescription,
            calendarId
        )
    }
    fun setDateTime(dateTime: ZonedDateTime) {
        day = dateTime.dayOfMonth
        month = dateTime.monthValue
        year = dateTime.year
        hour = dateTime.hour
        minute = dateTime.minute
    }

    fun submitDateEvent() = viewModelScope.launch {
        val dateEvent = createThisDateEvent()
        if (id.isNullOrEmpty()) {
            EventDatabase.insertDateEvent(dateEvent)
        } else {
            EventDatabase.updateDateEvent(id!!, dateEvent)
        }
    }

    fun getEventFromFirebase(eventId: String, callback: (DateEvent?) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val eventRef = database.getReference("date_events").child(eventId.toString())

        eventRef.get().addOnSuccessListener { dataSnapshot ->
            val dateEvent = dataSnapshot.getValue(DateEvent::class.java)
            callback(dateEvent)
        }.addOnFailureListener {
            callback(null)
        }
    }
}
