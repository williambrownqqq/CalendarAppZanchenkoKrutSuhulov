package com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.EventDatabase
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
class MainActivityViewModel(private val application: Application): ViewModel() {

    var currentDate: ZonedDateTime = ZonedDateTime.now()
    var monthEvents: LiveData<List<DateEvent>> = MutableLiveData()

    fun getMonthEvents() {
        val year = currentDate.year
        val month = currentDate.monthValue
        val liveData = monthEvents as MutableLiveData
        Log.d("MainActivityViewModel", "!EVENTS getMonthEvents")

        EventDatabase.getMonthEvents(year, month) { events ->
            liveData.postValue(events)
        }

        Log.d("MainActivityViewModel", "!EVENTS getMonthEvents $monthEvents")
    }
//    private fun setupDateEventViewModel() {
//        val dateEventDao = DateEventDatabase.getInstance(application).dateEventDao()
//        val dateEventViewModelFactory = DateEventViewModelFactory(dateEventDao)
//        dateEventViewModel = ViewModelProvider(
//            activity,
//            dateEventViewModelFactory
//        )[DateEventViewModel::class.java]
//    }

}
