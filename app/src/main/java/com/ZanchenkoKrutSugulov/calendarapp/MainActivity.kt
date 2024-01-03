package com.ZanchenkoKrutSugulov.calendarapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.CalendarDay
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.EventDatabase
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.CalendarRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.EventsRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMonthsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getYearsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.localDateToEpochSecond
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.MainActivityViewModel
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.DateEventViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonShowProfile: ImageButton
    private lateinit var buttonMenu: ImageButton
    private var currentUser: FirebaseUser? = null
    private lateinit var activityViewModel: MainActivityViewModel
    private var monthEvents: LiveData<List<DateEvent>> = MutableLiveData()


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

//        setupActivityViewModel()
//        setupSpinners()

    }

    override fun onStart() {
        super.onStart()
//        getMonthEvents()
    }

    private fun setupActivityViewModel() {
        activityViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        observeMonthEvents()
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
        Log.d("MainActivity", "!EVENTS getMonthEvents")
//        activityViewModel.getMonthEvents()
//        observeMonthEvents()
        var currentDate: ZonedDateTime = ZonedDateTime.now()
        val year = currentDate.year
        val month = currentDate.monthValue
        val liveData = monthEvents as MutableLiveData

        EventDatabase.getMonthEvents(year, month) { events ->
            liveData.postValue(events)
        }
    }

    private fun setupSpinners() {
        val monthSpinner = findViewById<Spinner>(R.id.spnMonth)
        val yearSpinner = findViewById<Spinner>(R.id.spnYear)

        monthSpinner.adapter = ArrayAdapter(this, R.layout.custom_spinner, getMonthsArray())
        yearSpinner.adapter = ArrayAdapter(this, R.layout.custom_spinner, getYearsArray())

        monthSpinner.setSelection(activityViewModel.currentDate.monthValue - 1)
        yearSpinner.setSelection(activityViewModel.currentDate.year - 2000)

//        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                activityViewModel.currentDate = activityViewModel.currentDate.withMonth(position + 1)
//                getMonthEvents()
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//
//        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                activityViewModel.currentDate = activityViewModel.currentDate.withYear(position + 2000)
//                getMonthEvents()
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
    }


    fun setupCalendarView() {
        val calendarRecycleView = findViewById<RecyclerView>(R.id.rvCalendar)
        calendarRecycleView.layoutManager = GridLayoutManager(this, 7)
        calendarRecycleView.adapter = activityViewModel.monthEvents.value?.let { dateEvents ->
            CalendarRecycleViewAdapter(activityViewModel.currentDate, { calendarDay ->
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
        eventRecyclerView.adapter = EventsRecycleViewAdapter(dateEvents, this@MainActivity) {dateEvent ->
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
        val dateEventViewModel = ViewModelProvider(this).get(DateEventViewModel::class.java)
        dateEventViewModel.deleteDateEvent(dateEvent.id)
    }
}