package com.ZanchenkoKrutSugulov.calendarapp.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.ZanchenkoKrutSugulov.calendarapp.R
import com.ZanchenkoKrutSugulov.calendarapp.utils.epochSecondToLocalDate
import com.ZanchenkoKrutSugulov.calendarapp.utils.getDaysArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getHourArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMinuteArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMonthsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getYearsArray
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.createEventActivity.CreateEventViewModel
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.createEventActivity.CreateEventViewModelFactory
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
open class CreateEventActivity: AppCompatActivity() {

    lateinit var activityViewModel: CreateEventViewModel
    private var startDateTime: ZonedDateTime = ZonedDateTime.now()
    private var dateTime: ZonedDateTime = startDateTime

    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText

    private lateinit var daySpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var hourSpinner: Spinner
    private lateinit var minuteSpinner: Spinner

    private var useTime = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        setupViewModel()


        getIntentExtras()
        setupUi()
    }

    private fun setupUi() {
        setupEditText()
        setupSpinners()
        setupButtons()
        setupSwitches()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getIntentExtras() {
        if (intent == null) return
        val epochSecond = intent.getLongExtra("date", 0)

        startDateTime = epochSecondToLocalDate(epochSecond)
        dateTime = startDateTime
    }

    private fun setupEditText() {
        nameEditText = findViewById(R.id.etEventName)
        descriptionEditText = findViewById(R.id.etEventDescription)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupViewModel() {
        val factory = CreateEventViewModelFactory(application, this, dateTime)
        activityViewModel = ViewModelProvider(
            this,
            factory
        )[CreateEventViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupSpinners() {
        daySpinner = findViewById(R.id.spnDay)
        monthSpinner = findViewById(R.id.spnMonth)
        yearSpinner = findViewById(R.id.spnYear)
        hourSpinner = findViewById(R.id.spnHour)
        minuteSpinner = findViewById(R.id.spnMinute)

        val dayAdapter = ArrayAdapter(this, R.layout.custom_spinner, getDaysArray(dateTime.toLocalDate()))
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

    @RequiresApi(Build.VERSION_CODES.O)
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
        useTimeSwitch.setOnCheckedChangeListener { _, isChecked ->
            useTime = isChecked

            if (useTime) timeLayout.visibility = View.VISIBLE
            else timeLayout.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun resetButtonClick() {
        dateTime = startDateTime
        setupSpinners()
    }
}