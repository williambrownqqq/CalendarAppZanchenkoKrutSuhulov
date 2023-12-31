package com.ZanchenkoKrutSugulov.calendarapp

import CalendarAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.Calendar
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.CalendarDao
import com.google.firebase.auth.FirebaseAuth
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.CalendarDatabase

class CalendarActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var calendarDao: CalendarDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_actiivity)

        recyclerView = findViewById(R.id.recyclerViewCalendars)
        recyclerView.layoutManager = LinearLayoutManager(this)

        calendarDao = CalendarDatabase
        loadCalendars()
    }

    private fun onEditCalendar(calendar: Calendar) {
    }

    private fun onDeleteCalendar(calendar: Calendar) {
        calendarDao.deleteCalendar(calendar.calendarId)
        loadCalendars()
    }

    private fun loadCalendars() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        calendarDao.getCalendars(userId) { calendars ->
            calendarAdapter = CalendarAdapter(calendars, ::onEditCalendar, ::onDeleteCalendar)
            recyclerView.adapter = calendarAdapter
        }
    }

}