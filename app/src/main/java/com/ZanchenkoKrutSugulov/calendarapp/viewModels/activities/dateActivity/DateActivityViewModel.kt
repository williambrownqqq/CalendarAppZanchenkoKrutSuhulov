package com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.dateActivity

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ZanchenkoKrutSugulov.calendarapp.dao.DateEventDatabase
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.dateEvent.DateEventViewModel
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.dateEvent.DateEventViewModelFactory
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
class DateActivityViewModel(private val application: Application, private val activity: AppCompatActivity, private val date: ZonedDateTime): ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    lateinit var dateEventViewModel: DateEventViewModel
    lateinit var dateEvents: LiveData<List<DateEvent>>

    init {
        setupDateEventViewModel()
        getMonthEvents()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthEvents() {
        dateEvents = dateEventViewModel.getDayEvents(date)
    }

    private fun setupDateEventViewModel() {
        val dateEventDao = DateEventDatabase.getInstance(application).dateEventDao()
        val dateEventViewModelFactory = DateEventViewModelFactory(dateEventDao)
        dateEventViewModel = ViewModelProvider(
            activity,
            dateEventViewModelFactory
        )[DateEventViewModel::class.java]
    }


}