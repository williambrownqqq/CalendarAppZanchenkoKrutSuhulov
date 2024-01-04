package com.ZanchenkoKrutSugulov.calendarapp

import java.util.*
import android.app.PendingIntent
import com.ZanchenkoKrutSugulov.calendarapp.NotificationReceiver
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.EventDatabase
import com.google.android.material.switchmaterial.SwitchMaterial
import com.ZanchenkoKrutSugulov.calendarapp.utils.epochSecondToLocalDate
import com.ZanchenkoKrutSugulov.calendarapp.utils.getDaysArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getHourArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMinuteArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMonthsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getPrimaryCalendarForUser
import com.ZanchenkoKrutSugulov.calendarapp.utils.getYearsArray
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.time.ZonedDateTime
import java.util.UUID


@RequiresApi(Build.VERSION_CODES.O)
class CreateEventActivity : AppCompatActivity() {
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val alarmManager by lazy {
        getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

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




    private var eventName = ""
    private var eventDescription = ""
    private var calendarId = ""
    var id: String? = null

    private var day = dateTime.dayOfMonth
    private var month = dateTime.monthValue
    private var year = dateTime.year

    private var hour: Int? = null
    private var minute: Int? = null



    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.let {
            getPrimaryCalendarForUser(it.uid) { calendarEvent ->
                if (calendarEvent != null) {
                    calendarId = calendarEvent.calendarId
                    Log.d("CalendarId", "Primary calendar ID: $calendarId")
                } else {
                    Log.e("CalendarError", "Primary calendar not found or error occurred")
                }
            }
        }

        setupViewModel()
        createNotificationChannel()
        getIntentExtras()
        setupUi()
    }

    private fun setupUi() {
        setupEditText()
        setupSpinners()
        setupButtons()
        setupSwitches()
    }

    private fun getIntentExtras() {
        val epochSecond = intent.getLongExtra("date", 0)
        if (epochSecond != 0L) {
            startDateTime = epochSecondToLocalDate(epochSecond)
            dateTime = startDateTime
        }
    }

    private fun setupEditText() {
        nameEditText = findViewById(R.id.etEventName)
        descriptionEditText = findViewById(R.id.etEventDescription)
    }

    private fun setupViewModel() {
        setDateTime(dateTime)
    }

    private fun setDateTime(dateTime: ZonedDateTime) {
        day = dateTime.dayOfMonth
        month = dateTime.monthValue
        year = dateTime.year
        hour = dateTime.hour
        minute = dateTime.minute
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
        useTimeSwitch.setOnCheckedChangeListener { _, isChecked ->
            useTime = isChecked

            if (useTime) timeLayout.visibility = View.VISIBLE
            else timeLayout.visibility = View.GONE
        }
    }

    private fun saveButtonClick() {
        eventName = nameEditText.text.toString()
        eventDescription = descriptionEditText.text.toString()

        day = daySpinner.selectedItem.toString().toInt()
        month = monthSpinner.selectedItemPosition + 1
        year = yearSpinner.selectedItem.toString().toInt()
        if (useTime) {
            hour = hourSpinner.selectedItem.toString().toInt()
            minute = minuteSpinner.selectedItem.toString().toInt()
        }
        val currentTimeMillis = System.currentTimeMillis()
        setNotification(currentTimeMillis)

        val dateEvent = createThisDateEvent()

        if (id.isNullOrEmpty()) {
            EventDatabase.insertDateEvent(dateEvent)
        } else {
            EventDatabase.updateDateEvent(id!!, dateEvent)
        }
        finish()
    }
    private fun createThisDateEvent(): DateEvent {
        return DateEvent(
            id ?: UUID.randomUUID().toString(),
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
        setupSpinners()
    }

    private fun setNotification(eventTime: Long) {
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val milliseconds = 10000
        val eventTime = System.currentTimeMillis().toLong() + milliseconds.toLong()
        Log.d("ALARM", "ALARM created: " + eventTime + pendingIntent.toString());
        alarmManager.set(AlarmManager.RTC_WAKEUP, eventTime, pendingIntent)
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "event_channel",
                "Event Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
            Log.d("CHANNEL", "Channel created: " + channel.toString());
        }
    }

}