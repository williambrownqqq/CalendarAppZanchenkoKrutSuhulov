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
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.CalendarRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.EventsRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMonthsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getYearsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.localDateToEpochSecond
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.mainActivity.MainActivityViewModel
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.mainActivity.MainActivityViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.Collections;


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonShowProfile: ImageButton
    private lateinit var buttonMenu: ImageButton
    private var currentUser: FirebaseUser? = null
    private lateinit var activityViewModel: MainActivityViewModel
    private var calendarService: Calendar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        buttonShowProfile = findViewById(R.id.buttonShowProfile)
        buttonMenu = findViewById(R.id.buttonOpenCalendars)
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

            fetchCalendarEvents(calendarService)
        }
    }

    private fun fetchCalendarEvents(calendarService: Calendar) {
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