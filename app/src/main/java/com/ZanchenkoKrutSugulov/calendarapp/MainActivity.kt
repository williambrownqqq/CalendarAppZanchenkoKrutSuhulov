package com.ZanchenkoKrutSugulov.calendarapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.CalendarDay
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.EventDatabase
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.CalendarRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.EventsRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMonthsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getPrimaryCalendarForUser
import com.ZanchenkoKrutSugulov.calendarapp.utils.getYearsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.localDateToEpochSecond
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.util.DateTime
import java.time.Instant
import java.time.ZoneId
import com.google.api.services.calendar.model.CalendarListEntry
import androidx.appcompat.app.AlertDialog
import com.google.api.services.calendar.model.Event


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonShowProfile: ImageButton
    private lateinit var buttonMenu: ImageButton
    private var currentUser: FirebaseUser? = null
    private var monthEvents: LiveData<List<DateEvent>> = MutableLiveData()
    var currentDate: ZonedDateTime = ZonedDateTime.now()
    private lateinit var syncButton: Button

    private var calendarService: Calendar? = null

    private var calendarId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        buttonShowProfile = findViewById(R.id.buttonShowProfile)
        buttonMenu = findViewById(R.id.buttonOpenCalendars)
        currentUser = auth.currentUser
        syncButton = findViewById(R.id.buttonSyncWithGoogle)


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

        buttonShowProfile.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        buttonMenu.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        if (currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        syncButton.setOnClickListener {
            listCalendars()
        }

        setupGoogleCalendarApi()

        setupSpinners()
    }

    override fun onStart() {
        super.onStart()
        if (calendarId == "") {
            FirebaseAuth.getInstance().currentUser?.let {
                getPrimaryCalendarForUser(it.uid) { calendarEvent ->
                    if (calendarEvent != null) {
                        getMonthEvents(calendarEvent.calendarId)
                        Log.d("CalendarId", "Primary calendar ID: ${calendarEvent.calendarId}")
                    } else {
                        Log.e("CalendarError", "Primary calendar not found or error occurred")
                    }
                }
            }
        } else {
            getMonthEvents(calendarId)
        }
        observeMonthEvents()
    }
    private fun observeMonthEvents() {
        monthEvents.observe(this) { dateEvents ->
            if (dateEvents != null) {
                setupCalendarView()
                setupEventView(dateEvents)
            }
        }
    }

    fun getMonthEvents(calendarID: String) {


        Log.d("getMonthEvents", "!monthEvents: ${monthEvents.map { it }}")
        Log.d("getMonthEvents", "!monthEvents: calendarId ${this.calendarId}")

        val year = currentDate.year
        val month = currentDate.monthValue
        val liveData = monthEvents as MutableLiveData

        EventDatabase.getMonthEvents(year, month, calendarID) { events ->
            liveData.postValue(events)
        }
        Log.d("getMonthEvents", "!monthEvents: ${monthEvents.value?.map { it }}")
        observeMonthEvents()
    }


    private fun setupGoogleCalendarApi() {
        val credential = GoogleAccountCredential.usingOAuth2(
            this, listOf(CalendarScopes.CALENDAR_READONLY)
        ).setBackOff(ExponentialBackOff())

        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
        credential.selectedAccount = googleSignInAccount?.account

        calendarService = Calendar.Builder(
            NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(getString(R.string.app_name))
            .build()
        Log.d("!GOOGLE API", "!GOOGLE API SERVICE: ${calendarService}")

    }
    private fun listCalendars() {
        val thread = Thread {
            try {
                val calendarList = calendarService?.calendarList()?.list()?.execute()
                val calendars = calendarList?.items
                Log.d("MainActivity", "!CALENDARS SYNC: ${calendars?.map { it.id }}")

                runOnUiThread {
                    if (calendars != null) {
                        showCalendarDialog(calendars)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error fetching calendar list: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        thread.start()
    }

    private fun showCalendarDialog(calendars: List<CalendarListEntry>) {
        val calendarNames = calendars.map { it.summary }.toTypedArray()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a Calendar")
        builder.setItems(calendarNames) { _, which ->
            loadEventsFromGoogleCalendar(calendars[which].id)
        }
        builder.show()
    }

    private fun fetchEventsFromCalendar(calendarId: String, year: Int, month: Int) {
        val thread = Thread {
            try {
                val startOfMonth = java.time.LocalDate.of(year, month, 1).toString() + "T00:00:00-07:00"
                val endOfMonth = java.time.LocalDate.of(year, month, 1).withDayOfMonth(
                    java.time.LocalDate.of(year, month, 1).lengthOfMonth()
                ).toString() + "T23:59:59-07:00"
                val events = calendarService?.events()
                    ?.list(calendarId)
                    ?.setTimeMin(DateTime(startOfMonth))
                    ?.setTimeMax(DateTime(endOfMonth))
                    ?.execute()

                Log.d("MainActivity", "!CALENDARS SYNC: Events: ${events?.items?.map { it}}")


                val items = events?.items
                if (items != null) {
                    if (items.isNotEmpty()) {
                        for (event in items) {
                            val eventStart = event.start.dateTime ?: event.start.date
                            val localDate: LocalDateTime = getLocalDateTime(eventStart)
                            Log.d("MainActivity", "!CALENDARS SYNC: Converting: $localDate")
                            val newEvent = convertEventFromGoogleCalendar(localDate, event)
                            Log.d("MainActivity", "!CALENDARS SYNC: newEvent: ${newEvent.toString()}")

                            if (newEvent != null) {
                                EventDatabase.insertDateEvent(newEvent)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error fetching events: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                    Log.d("MainActivity", "!CALENDARS SYNC: Error fetching events: ${e.message}")

                }
            }
        }
        thread.start()
    }
    private fun getLocalDateTime(eventStart: DateTime): LocalDateTime {
        return Instant.ofEpochMilli(eventStart.value).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }
    private fun convertEventFromGoogleCalendar(localDate: LocalDateTime?, event: Event): DateEvent? {
        Log.d("MainActivity", "!CALENDARS SYNC: convertEventFromGoogleCalendar: ${localDate?.year}")
        return localDate?.let {
            DateEvent(
                year = it.year,
                month = it.month.value,
                day = it.dayOfMonth,
                hour = it.hour,
                minute = it.minute,
                name = event.summary,
                description = event.description,
                calendarId = calendarId,
                id = UUID.randomUUID().toString(),
            )
        }
    }

    private fun loadEventsFromGoogleCalendar(calendarId: String) {
        val selectedYear = currentDate.year
        val selectedMonth = currentDate.monthValue
        fetchEventsFromCalendar(calendarId, selectedYear, selectedMonth)
    }


    private fun setupSpinners() {
        val monthSpinner = findViewById<Spinner>(R.id.spnMonth)
        val yearSpinner = findViewById<Spinner>(R.id.spnYear)

        monthSpinner.adapter = ArrayAdapter(this, R.layout.custom_spinner, getMonthsArray())
        yearSpinner.adapter = ArrayAdapter(this, R.layout.custom_spinner, getYearsArray())
        monthSpinner.setSelection(currentDate.monthValue - 1)
        yearSpinner.setSelection(currentDate.year - 2000)

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                currentDate = currentDate.withMonth(position + 1)
                getMonthEvents(calendarId)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                currentDate = currentDate.withYear(position + 2000)
                getMonthEvents(calendarId)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupCalendarView() {
        val calendarRecycleView = findViewById<RecyclerView>(R.id.rvCalendar)
        calendarRecycleView.layoutManager = GridLayoutManager(this, 7)

        calendarRecycleView.adapter = monthEvents.value?.let { dateEvents ->
            CalendarRecycleViewAdapter(
                currentDate, { calendarDay ->
                    calendarDayClick(calendarDay)
                }, dateEvents,
                getColorStateList(R.color.white),
                getColorStateList(R.color.dayWithEvent),
                getColorStateList(R.color.black),
                getColorStateList(R.color.currentDayWithEvent)
            )
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setupEventView(dateEvents: List<DateEvent>) {
        val eventRecyclerView = findViewById<RecyclerView>(R.id.rvEvents)
        eventRecyclerView.layoutManager = LinearLayoutManager(this)
        eventRecyclerView.adapter =
            EventsRecycleViewAdapter(dateEvents, this@MainActivity) { dateEvent ->
                eventClearClick(dateEvent)
            }
        updateRecyclerViewHeight(eventRecyclerView, dateEvents)

        findViewById<TextView>(R.id.tvMonthEvents).text = "Events of months (${dateEvents.size})"
    }

    private fun updateRecyclerViewHeight(recyclerView: RecyclerView, dateEvents: List<DateEvent>) {
        val itemCountToShow = if (dateEvents.size <= 3) dateEvents.size else 3

        val scale = recyclerView.resources.displayMetrics.density
        recyclerView.layoutParams.height = (90 * itemCountToShow * scale * 2).toInt()
        recyclerView.requestLayout()
    }

    private fun calendarDayClick(calendarDay: CalendarDay) {
        val intent = Intent(this@MainActivity, DateActivity::class.java)
        intent.putExtra("date", localDateToEpochSecond(calendarDay.date))
        startActivity(intent)
    }

    private fun eventClearClick(dateEvent: DateEvent) {
        EventDatabase.deleteDateEvent(dateEvent.id)
    }
}