package com.ZanchenkoKrutSugulov.calendarapp

import CalendarAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.Calendar

class CalendarActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_actiivity)

        recyclerView = findViewById(R.id.recyclerViewCalendars)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val calendars = loadCalendars()

        calendarAdapter = CalendarAdapter(calendars, ::onEditCalendar, ::onDeleteCalendar)
        recyclerView.adapter = calendarAdapter
    }

    private fun onEditCalendar(calendar: Calendar) {
    }

    private fun onDeleteCalendar(calendar: Calendar) {
    }

    private fun loadCalendars(): List<Calendar> {
        return listOf()
    }
}