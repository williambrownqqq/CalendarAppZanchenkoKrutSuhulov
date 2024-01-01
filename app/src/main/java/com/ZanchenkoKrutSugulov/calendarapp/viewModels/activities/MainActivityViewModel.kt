package com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.EventDatabase
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
class MainActivityViewModel(private val application: Application): ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    var currentDate: ZonedDateTime = ZonedDateTime.now()
    var monthEvents: LiveData<List<DateEvent>> = MutableLiveData()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthEvents() {
        val year = currentDate.year
        val month = currentDate.monthValue
        val liveData = monthEvents as MutableLiveData
        EventDatabase.getMonthEvents(year, month) { events ->
            liveData.postValue(events)
        }
    }
}
