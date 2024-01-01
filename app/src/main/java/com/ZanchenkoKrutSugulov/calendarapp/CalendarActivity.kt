package com.ZanchenkoKrutSugulov.calendarapp

import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.CalendarAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.DeadObjectException
import android.util.Log
import android.widget.ImageView
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
    private lateinit var backButton: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_actiivity)

        backButton = findViewById(R.id.backFromCalendarsList)

        recyclerView = findViewById(R.id.recyclerViewCalendars)
        recyclerView.layoutManager = LinearLayoutManager(this)

        calendarDao = CalendarDatabase
        loadCalendars()

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun onEditCalendar(calendar: Calendar) {
    }

    private fun onDeleteCalendar(calendar: Calendar) {
        calendar.calendarId.let { calendarDao.deleteCalendar(it) }
        loadCalendars()
    }

    private fun loadCalendars() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        calendarDao.getCalendars(userId) { calendars ->
            calendarAdapter = CalendarAdapter(calendars, ::onEditCalendar, ::onDeleteCalendar)
            recyclerView.adapter = calendarAdapter
        }
    }
    override fun onDestroy() {
        try {
            super.onDestroy()
        } catch (e: DeadObjectException) {
            Log.e("CalendarActivity", "Error in onDestroy: ${e.message}")
        }
    }
}