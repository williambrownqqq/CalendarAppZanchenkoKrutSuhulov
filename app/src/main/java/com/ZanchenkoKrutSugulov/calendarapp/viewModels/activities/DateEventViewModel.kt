package com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.EventDatabase
import kotlinx.coroutines.launch

class DateEventViewModel() : ViewModel() {
    fun insertDateEvent(dateEvent: DateEvent) = viewModelScope.launch {
        EventDatabase.insertDateEvent(dateEvent)
    }

    fun deleteDateEvent(eventId: String) = viewModelScope.launch {
        EventDatabase.deleteDateEvent(eventId)
    }

    fun updateDateEvent(eventId: String, dateEvent: DateEvent) = viewModelScope.launch {
        EventDatabase.updateDateEvent(eventId, dateEvent)
    }

    fun getMonthEvents(year: Int, month: Int): LiveData<List<DateEvent>> {
        val liveData = MutableLiveData<List<DateEvent>>()
        EventDatabase.getMonthEvents(year, month) { events ->
            liveData.postValue(events)
        }
        return liveData
    }

    fun getDayEvents(year: Int, month: Int, day: Int): LiveData<List<DateEvent>> {
        val liveData = MutableLiveData<List<DateEvent>>()
        EventDatabase.getDateEvents(year, month, day) { events ->
            liveData.postValue(events)
        }
        return liveData
    }

    fun getEvent(eventId: String): LiveData<DateEvent> {
        val liveData = MutableLiveData<DateEvent>()
        EventDatabase.getDateEvent(eventId) { event ->
            liveData.postValue(event)
        }
        return liveData
    }
}

//    fun insertDateEvent(dateEvent: DateEvent) = viewModelScope.launch {
//        dateEventDao.insertDateEvent(dateEvent)
//        saveDateEventToFirebase(dateEvent)
//    }
//
//    fun deleteDateEvent(dateEvent: DateEvent) = viewModelScope.launch {
//        dateEventDao.deleteDateEvent(dateEvent)
//        deleteDateEventFromFirebase(dateEvent.id.toString())
//    }
//
//    fun updateDateEvent(dateEvent:DateEvent) = viewModelScope.launch {
//        dateEventDao.updateDateEvent(dateEvent)
//        updateDateEventInFirebase(dateEvent.id.toString(), dateEvent)
//    }
//
//    fun getMonthEvents(year: Int, month: Int): LiveData<List<DateEvent>> {
//        val liveData = MutableLiveData<List<DateEvent>>()
//        dateEventDao.getMonthEvents(year, month) { events ->
//            liveData.postValue(events)
//        }
//        return liveData
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun getDayEvents(date: ZonedDateTime): LiveData<List<DateEvent>> {
//        return dateEventDao.getDateEvents(date.year, date.monthValue, date.dayOfMonth)
//    }
//
//    fun getEvent(eventId: Int): LiveData<DateEvent> {
//        return dateEventDao.getDateEvent(eventId)
//    }
//}