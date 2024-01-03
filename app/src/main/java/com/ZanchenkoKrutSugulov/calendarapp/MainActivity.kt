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
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.CalendarDatabase
import com.ZanchenkoKrutSugulov.calendarapp.firebaseDB.FirebaseRealTimeDatabase
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.CalendarRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.EventsRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.utils.createPrimaryCalendarForNewUser
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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


import java.util.Collections;
import java.util.Locale


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
            getCalendarEvents()
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
                val startOfMonth = java.time.LocalDate.of(year, month, 1)
                val endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth())

                val events = calendarService?.events()
                    ?.list(calendarId)
                    ?.setTimeMin(DateTime(startOfMonth.toString()))
                    ?.setTimeMax(DateTime(endOfMonth.toString()))
                    ?.execute()

                val items = events?.items
                if (items != null) {
                    if (items.isNotEmpty()) {
                        for (event in items) {
                            val eventStart = event.start.dateTime ?: event.start.date
                            val localDate: LocalDateTime? = if (eventStart != null) {
                                Instant.ofEpochMilli(eventStart.value).atZone(ZoneId.systemDefault()).toLocalDateTime()
                            } else null
                            val newEvent = localDate?.let {
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
                }
            }
        }
        thread.start()
    }

    private fun loadEventsFromGoogleCalendar() {
        val selectedYear = activityViewModel.currentDate.year
        val selectedMonth = activityViewModel.currentDate.monthValue

        auth.currentUser?.let {
            CalendarDatabase.getUserPrimaryCalendar(it.uid) { hasPrimary ->
                if (!hasPrimary) {
                    Log.d("UserUtils", "!hasPrimary: $hasPrimary")
                }
            }
        }
        val calendarId = "primary"

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