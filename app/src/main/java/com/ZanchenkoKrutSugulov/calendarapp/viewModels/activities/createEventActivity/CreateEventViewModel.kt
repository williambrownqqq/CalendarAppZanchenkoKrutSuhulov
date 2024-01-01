package com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.createEventActivity

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.DateEventDatabase
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.dateEvent.DateEventViewModel
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.dateEvent.DateEventViewModelFactory
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
@RequiresApi(Build.VERSION_CODES.O)
class CreateEventViewModel(private val application: Application, private val activity: AppCompatActivity, val date: ZonedDateTime): ViewModel() {
    lateinit var dateEventViewModel: DateEventViewModel

    var eventName = ""
    var eventDescription = ""
    var eventId: Int? = null


    var day = date.dayOfMonth
    var month = date.monthValue
    var year = date.year

    var hour: Int? = null
    var minute: Int? = null

    init {
        setupDateEventViewModel()
    }

    private fun setupDateEventViewModel() {
        val dateEventDao = DateEventDatabase.getInstance(application).dateEventDao()
        val dateEventViewModelFactory = DateEventViewModelFactory(dateEventDao)
        dateEventViewModel = ViewModelProvider(
            activity,
            dateEventViewModelFactory
        )[DateEventViewModel::class.java]
    }

    fun createThisDateEvent(): DateEvent {
        return DateEvent(
            eventId ?: 0,
            year,
            month,
            day,
            hour,
            minute,
            eventName,
            eventDescription
        )
    }

    fun submitDateEvent() =  viewModelScope.launch {
        if (eventId == null)  {
            insertEvent()
            return@launch
        }
        replaceEvent()
    }

    fun replaceEvent() = viewModelScope.launch {
        val dateEvent = createThisDateEvent()
        dateEventViewModel.updateDateEvent(dateEvent)
    }

    fun insertEvent() = viewModelScope.launch {
        val dateEvent = createThisDateEvent()
        dateEventViewModel.insertDateEvent(dateEvent)
    }
}