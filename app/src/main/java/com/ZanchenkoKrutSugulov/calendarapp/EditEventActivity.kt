package com.ZanchenkoKrutSugulov.calendarapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.EventDatabase
import com.ZanchenkoKrutSugulov.calendarapp.utils.getDaysArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getHourArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMinuteArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMonthsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getYearsArray
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class EditEventActivity : AppCompatActivity() {
    private var startDateTime: ZonedDateTime = ZonedDateTime.now()
    private var dateTime: ZonedDateTime = startDateTime

    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText

    private lateinit var daySpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var hourSpinner: Spinner
    private lateinit var minuteSpinner: Spinner

    private var dateEvent: DateEvent? = null
    private var useTime = false


    private var eventName = ""
    private var eventDescription = ""
    private var calendarId = ""
    var id: String? = null

    private var day = dateTime.dayOfMonth
    var month = dateTime.monthValue
    var year = dateTime.year

    private var hour: Int? = null
    private var minute: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        setupViewModel()

        getIntentExtras()
        getEventInformation()
    }

    private fun getIntentExtras() {
        if (intent == null) return
        id = intent.getStringExtra("id")
        Log.d("EditEventActivity", "!edit event - getIntentExtras id ${id}")

    }

    private fun getEventInformation() {
        val eventId = id ?: return

        getEventFromFirebase(eventId) { dateEvent ->
            runOnUiThread {

                Log.d("EditEventActivity", "!edit event - getEventInformation ${dateEvent}")

                if (dateEvent != null) {
                    onDateEventLoad(dateEvent)
                }
            }
        }
    }

    private fun getEventFromFirebase(eventId: String, callback: (DateEvent?) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val eventRef = database.getReference("date_events")
        val query = eventRef.orderByChild("id").equalTo(eventId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dateEvent = dataSnapshot.children.firstOrNull()?.getValue(DateEvent::class.java)
                callback(dateEvent)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("EditEventActivity", "Error getting event: ${databaseError.message}")
                callback(null)
            }
        })
    }


    private fun onDateEventLoad(dateEvent: DateEvent) {
        this.dateEvent = dateEvent

        Log.d("EditEventActivity", "!edit event - onDateEventLoad  ${dateEvent.id} - ${this.dateEvent}")
//        id = dateEvent.id

        Log.d("EditEventActivity", "!edit event: ${this.dateEvent}")

        if (dateEvent.hour != null || dateEvent.minute != null) useTime = true

        val hour = if (dateEvent.hour == null) 0 else dateEvent.hour!!
        val minute = if (dateEvent.minute == null) 0 else dateEvent.minute!!

        startDateTime = ZonedDateTime.of(
            dateEvent.year,
            dateEvent.month,
            dateEvent.day,
            hour,
            minute,
            0,
            0,
            ZoneId.systemDefault()
        )

        dateTime = startDateTime


        setupUi()
    }

    private fun setupUi() {
        setupTextView()
        setupEditText()
        setupSpinners()
        setupButtons()
        setupSwitches()
    }

    private fun setupTextView() {
        val tvCreateEvent = findViewById<TextView>(R.id.tvCreateEvent)
        tvCreateEvent.text = resources.getString(R.string.edit_event)
    }

    private fun setupEditText() {
        nameEditText = findViewById(R.id.etEventName)
        descriptionEditText = findViewById(R.id.etEventDescription)

        nameEditText.setText(dateEvent?.name ?: "")
        descriptionEditText.setText(dateEvent?.description ?: "")
    }

    private fun setupViewModel() {
        getEventInformation()
    }

    private fun setupSpinners() {
        daySpinner = findViewById(R.id.spnDay)
        monthSpinner = findViewById(R.id.spnMonth)
        yearSpinner = findViewById(R.id.spnYear)
        hourSpinner = findViewById(R.id.spnHour)
        minuteSpinner = findViewById(R.id.spnMinute)

        val dayAdapter =
            ArrayAdapter(this, R.layout.custom_spinner, getDaysArray(dateTime.toLocalDate()))
        val monthAdapter = ArrayAdapter(this, R.layout.custom_spinner, getMonthsArray())
        val yearAdapter = ArrayAdapter(this, R.layout.custom_spinner, getYearsArray())
        val hourAdapter = ArrayAdapter(this, R.layout.custom_spinner, getHourArray())
        val minuteAdapter = ArrayAdapter(this, R.layout.custom_spinner, getMinuteArray())

        daySpinner.adapter = dayAdapter
        monthSpinner.adapter = monthAdapter
        yearSpinner.adapter = yearAdapter
        hourSpinner.adapter = hourAdapter
        minuteSpinner.adapter = minuteAdapter

        daySpinner.setSelection(dateTime.dayOfMonth - 1)
        monthSpinner.setSelection(dateTime.monthValue - 1)
        yearSpinner.setSelection(dateTime.year - 2000)
        hourSpinner.setSelection(dateTime.hour)
        minuteSpinner.setSelection(dateTime.minute)
    }

    private fun setupButtons() {
        val saveButton = findViewById<Button>(R.id.btnSave)
        val resetButton = findViewById<Button>(R.id.btnReset)

        saveButton.setOnClickListener {
            saveButtonClick()
        }

        resetButton.setOnClickListener {
            resetButtonClick()
        }
    }

    private fun setupSwitches() {
        val useTimeSwitch = findViewById<SwitchMaterial>(R.id.swUseTime)
        val timeLayout = findViewById<LinearLayout>(R.id.lyTime)

        if (useTime) {
            useTimeSwitch.isChecked = true
            timeLayout.visibility = View.VISIBLE
        }

        useTimeSwitch.setOnCheckedChangeListener { _, isChecked ->
            useTime = isChecked

            if (useTime) timeLayout.visibility = View.VISIBLE
            else timeLayout.visibility = View.GONE
        }
    }

    private fun saveButtonClick() {
        eventName = nameEditText.text.toString()
        eventDescription = descriptionEditText.text.toString()

        year = yearSpinner.selectedItem.toString().toInt()
        month = monthSpinner.selectedItemPosition + 1
        day = daySpinner.selectedItem.toString().toInt()

        if (useTime) {
            hour = hourSpinner.selectedItem.toString().toInt()
            minute = minuteSpinner.selectedItem.toString().toInt()
        }

        val dateEvent = createThisDateEvent()
        if (this.dateEvent?.id.isNullOrEmpty()) {
            Log.d("EditEventActivity", "!edit event: id.isNullOrEmpty() -  ${id}")
            EventDatabase.insertDateEvent(dateEvent)
        } else {
            Log.d("EditEventActivity", "!edit event: id - ${id}")
            EventDatabase.updateDateEvent(this.dateEvent?.id!!, dateEvent)
        }
        this.finish()
    }

    private fun createThisDateEvent(): DateEvent {
        return DateEvent(
            this.dateEvent?.id ?: UUID.randomUUID().toString(),
            year,
            month,
            day,
            hour,
            minute,
            eventName,
            eventDescription,
            calendarId
        )
    }

    private fun resetButtonClick() {
        dateTime = startDateTime
        setupUi()
    }
}