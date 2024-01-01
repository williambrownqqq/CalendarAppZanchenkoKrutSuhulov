package com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.ZonedDateTime
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.EventDatabase
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class DateActivityViewModel(application: Application, private var date: ZonedDateTime): ViewModel() {

    private val _dateEvents = MutableLiveData<List<DateEvent>>()
    val dateEvents: LiveData<List<DateEvent>> = _dateEvents

    init {
        getDayEvents()
        getMonthEvents()
    }

    fun getDateEvents(newDate: ZonedDateTime) {
        date = newDate
        getDayEvents()
    }

    private fun getDayEvents() {
        EventDatabase.getDateEvents(date.year, date.monthValue, date.dayOfMonth) { events ->
            _dateEvents.postValue(events)
        }
    }

    fun getMonthEvents() {
        EventDatabase.getMonthEvents(date.year, date.monthValue) { events ->
            _dateEvents.postValue(events)
        }
    }

    fun deleteDateEvent(dateEvent: DateEvent) = viewModelScope.launch {
        EventDatabase.deleteDateEvent(dateEvent.id)
    }
}
