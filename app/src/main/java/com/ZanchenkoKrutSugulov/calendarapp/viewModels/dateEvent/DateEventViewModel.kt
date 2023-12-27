package com.ZanchenkoKrutSugulov.calendarapp.viewModels.dateEvent

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.DateEventDao
import com.ZanchenkoKrutSugulov.calendarapp.firebaseDB.FirebaseRealTimeDatabase.deleteDateEventFromFirebase
import com.ZanchenkoKrutSugulov.calendarapp.firebaseDB.FirebaseRealTimeDatabase.saveDateEventToFirebase
import com.ZanchenkoKrutSugulov.calendarapp.firebaseDB.FirebaseRealTimeDatabase.updateDateEventInFirebase
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class DateEventViewModel(private val dateEventDao: DateEventDao): ViewModel() {

    fun insertDateEvent(dateEvent: DateEvent) = viewModelScope.launch {
        dateEventDao.insertDateEvent(dateEvent)
        saveDateEventToFirebase(dateEvent)
    }

    fun deleteDateEvent(dateEvent: DateEvent) = viewModelScope.launch {
        dateEventDao.deleteDateEvent(dateEvent)
        deleteDateEventFromFirebase(dateEvent.id.toString())
    }

    fun updateDateEvent(dateEvent:DateEvent) = viewModelScope.launch {
        dateEventDao.updateDateEvent(dateEvent)
        updateDateEventInFirebase(dateEvent.id.toString(), dateEvent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthEvents(date: ZonedDateTime): LiveData<List<DateEvent>> {
        return dateEventDao.getMonthEvents(date.year, date.monthValue)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayEvents(date: ZonedDateTime): LiveData<List<DateEvent>> {
        return dateEventDao.getDateEvents(date.year, date.monthValue, date.dayOfMonth)
    }

    fun getEvent(eventId: Int): LiveData<DateEvent> {
        return dateEventDao.getDateEvent(eventId)
    }
}