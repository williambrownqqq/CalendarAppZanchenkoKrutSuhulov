package com.ZanchenkoKrutSugulov.calendarapp

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.utils.getDaysArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getHourArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMinuteArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMonthsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getYearsArray
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.CreateEventViewModel
import java.time.ZoneId
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
class EditEventActivity : AppCompatActivity() {
    private lateinit var activityViewModel: CreateEventViewModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        setupViewModel()

        getIntentExtras()
        getEventInformation()
    }

    private fun getIntentExtras() {
        if (intent == null) return
        activityViewModel.id = intent.getStringExtra("id")
    }

    private fun getEventInformation() {
        val eventId = activityViewModel.id ?: return

        activityViewModel.getEventFromFirebase(eventId) { dateEvent ->
            runOnUiThread {
                if (dateEvent != null) {
                    onDateEventLoad(dateEvent)
                }
            }
        }
    }


    private fun onDateEventLoad(dateEvent: DateEvent) {
        this.dateEvent = dateEvent

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
        activityViewModel = ViewModelProvider(this)[CreateEventViewModel::class.java]
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
        monthSpinner.setSelection(activityViewModel.date.monthValue - 1)
        yearSpinner.setSelection(activityViewModel.date.year - 2000)
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
        activityViewModel.eventName = nameEditText.text.toString()
        activityViewModel.eventDescription = descriptionEditText.text.toString()

        activityViewModel.year = yearSpinner.selectedItem.toString().toInt()
        activityViewModel.month = monthSpinner.selectedItemPosition + 1
        activityViewModel.day = daySpinner.selectedItem.toString().toInt()

        if (useTime) {
            activityViewModel.hour = hourSpinner.selectedItem.toString().toInt()
            activityViewModel.minute = minuteSpinner.selectedItem.toString().toInt()
        }

        activityViewModel.submitDateEvent()
        this.finish()
    }

    private fun resetButtonClick() {
        dateTime = startDateTime
        setupUi()
    }
}