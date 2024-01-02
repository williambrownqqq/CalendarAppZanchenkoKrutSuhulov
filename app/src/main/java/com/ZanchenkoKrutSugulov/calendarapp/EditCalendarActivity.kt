package com.ZanchenkoKrutSugulov.calendarapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.Calendar
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.CalendarDatabase

class EditCalendarActivity : AppCompatActivity() {
    private lateinit var calendarNameEditText: EditText
    private lateinit var primarySwitch: Switch
    private lateinit var saveButton: Button
    private lateinit var resetButton: Button
    private var originalCalendar: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_calendar)
        val calendarId = intent.getStringExtra("calendarId") ?: return

        calendarNameEditText = findViewById(R.id.editTextCalendarName)
        primarySwitch = findViewById(R.id.switchPrimary)
        saveButton = findViewById(R.id.buttonSave)
        resetButton = findViewById(R.id.buttonReset)
        loadCalendarData(calendarId)

//        originalCalendar = CalendarDatabase.getCalendar(intent.getSerializableExtra("calendarId"))
        displayCalendarData()

        saveButton.setOnClickListener {
            saveCalendarData()
        }

        resetButton.setOnClickListener {
            resetCalendarData()
        }
    }
    private fun loadCalendarData(calendarId: String) {
        CalendarDatabase.getCalendar(calendarId) { calendar ->
            originalCalendar = calendar
            displayCalendarData()
        }
    }

    private fun displayCalendarData() {
        originalCalendar?.let {
            calendarNameEditText.setText(it.name)
            primarySwitch.isChecked = it.primary
        }
    }

    private fun saveCalendarData() {
        val updatedCalendar = originalCalendar?.copy(
            name = calendarNameEditText.text.toString(),
            primary = primarySwitch.isChecked
        )

        updatedCalendar?.let {
            CalendarDatabase.updateCalendar(it)
            Toast.makeText(this, "Calendar updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetCalendarData() {
        displayCalendarData()
    }
}