package com.ZanchenkoKrutSugulov.calendarapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ZanchenkoKrutSugulov.calendarapp.activities.DateActivity
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.CalendarDay
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.firebaseDB.FirebaseRealTimeDatabase
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.CalendarRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.EventsRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMonthsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getYearsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.localDateToEpochSecond
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.mainActivity.MainActivityViewModel
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.mainActivity.MainActivityViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.util.DateTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import com.google.api.services.calendar.model.CalendarListEntry
import androidx.appcompat.app.AlertDialog
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.DateEventDao
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.dateEvent.DateEventViewModel
import com.google.api.services.calendar.model.Event
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Collections;


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonShowProfile: ImageButton
    private lateinit var buttonMenu: ImageButton
    private lateinit var syncButton: Button
    private var currentUser: FirebaseUser? = null
    private lateinit var activityViewModel: MainActivityViewModel
    private var calendarService: Calendar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        buttonShowProfile = findViewById(R.id.buttonShowProfile)
        buttonMenu = findViewById(R.id.buttonOpenCalendars)
        syncButton = findViewById(R.id.buttonSyncWithGoogle)
        currentUser = auth.currentUser


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
        setupActivityViewModel()
        setupSpinners()

    }

    override fun onStart() {
        super.onStart()
        getMonthEvents()
    }

    private fun setupActivityViewModel() {
        val factory = MainActivityViewModelFactory(application, this)
        activityViewModel = ViewModelProvider(
            this,
            factory
        )[MainActivityViewModel::class.java]
    }

    private fun observeMonthEvents() {
        activityViewModel.monthEvents.observe(this) { dateEvents ->
            if (dateEvents != null) {
                setupCalendarView()
                setupEventView(dateEvents)
            }
        }
    }

    fun getMonthEvents() {
        activityViewModel.getMonthEvents()
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

    private fun getCalendarEvents() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singleton(CalendarScopes.CALENDAR)
            ).setBackOff(ExponentialBackOff())

            credential.selectedAccount = account.account

            val transport: HttpTransport = NetHttpTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName(getString(R.string.app_name))
                .build()

//            fetchEventsFromCalendar(calendarId, selectedYear, selectedMonth)
        }
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
                                FirebaseRealTimeDatabase.saveDateEventToFirebase(newEvent)
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
                id = 0
            )
        }
    }

    private fun loadEventsFromGoogleCalendar(calendarId: String) {
        val selectedYear = activityViewModel.currentDate.year
        val selectedMonth = activityViewModel.currentDate.monthValue
        fetchEventsFromCalendar(calendarId, selectedYear, selectedMonth)
    }


    private fun setupSpinners() {
        val monthSpinner = findViewById<Spinner>(R.id.spnMonth)
        val yearSpinner = findViewById<Spinner>(R.id.spnYear)

        val monthsAdapter = ArrayAdapter(this, R.layout.custom_spinner, getMonthsArray())
        val yearAdapter = ArrayAdapter(this, R.layout.custom_spinner, getYearsArray())

        monthSpinner.adapter = monthsAdapter
        yearSpinner.adapter = yearAdapter

        monthSpinner.setSelection(activityViewModel.currentDate.monthValue - 1)
        yearSpinner.setSelection(activityViewModel.currentDate.year - 2000)

        monthSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                activityViewModel.currentDate =
                    activityViewModel.currentDate.withMonth(position + 1)
                setupCalendarView()
                getMonthEvents()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

        yearSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                activityViewModel.currentDate =
                    activityViewModel.currentDate.withYear(position + 2000)
                setupCalendarView()
                getMonthEvents()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun setupCalendarView() {
        val calendarRecycleView = findViewById<RecyclerView>(R.id.rvCalendar)
        calendarRecycleView.layoutManager = GridLayoutManager(this, 7)
        calendarRecycleView.adapter = activityViewModel.monthEvents.value?.let { dateEvents ->
            CalendarRecycleViewAdapter(
                activityViewModel.currentDate, { calendarDay ->
                    calendarDayClick(
                        calendarDay
                    )
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
        activityViewModel.dateEventViewModel.deleteDateEvent(dateEvent)
    }
}