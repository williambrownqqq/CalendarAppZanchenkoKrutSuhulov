package com.ZanchenkoKrutSugulov.calendarapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.database.dao.EventDatabase
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.EventsRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.utils.epochSecondToLocalDate
import com.ZanchenkoKrutSugulov.calendarapp.utils.getDaysOfWeekArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMonthsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.localDateToEpochSecond
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
class DateActivity : AppCompatActivity() {
    private lateinit var date: ZonedDateTime

    private lateinit var backButton: ImageView
    private val _dateEvents = MutableLiveData<List<DateEvent>>()
    val dateEvents: LiveData<List<DateEvent>> = _dateEvents


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)

        getIntentExtras()
        setupActivityViewModel()

        setupButtons()
        setupTextViews()


        backButton = findViewById(R.id.backFromEventsDateList)

        backButton.setOnClickListener {
            finish()
        }

    }

    override fun onStart() {
        super.onStart()
        getDateEvents()
    }

    private fun getIntentExtras(): Boolean {
        val epochSecond = intent.getLongExtra("date", 0)
        return if (epochSecond != 0L) {
            date = epochSecondToLocalDate(epochSecond)
            true
        } else {
            false
        }
    }

    private fun setupActivityViewModel() {
        observeDateEvents()
        observeMonthEvents()
    }

    private fun observeDateEvents() {
        dateEvents.observe(this) { dateEvents ->
            setupEventView(dateEvents)
        }
        EventDatabase.getDateEvents(date.year, date.monthValue, date.dayOfMonth) { events ->
            _dateEvents.postValue(events)
        }
    }

    private fun observeMonthEvents() {
        dateEvents.observe(this) { dateEvents ->
            setupEventView(dateEvents)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupTextViews() {
        val currentDay = getDaysOfWeekArray()[date.dayOfWeek.value - 1]
        val dateText = findViewById<TextView>(R.id.tvDate)
        dateText.text =
            "${currentDay}, ${date.dayOfMonth} of ${getMonthsArray()[date.monthValue - 1]} ${date.year}"
    }

    private fun setupButtons() { //        val createEventButton = findViewById<Button>(R.id.btnCreateEvent)
        findViewById<Button>(R.id.btnCreateEvent).setOnClickListener {
            createEvent()
        }
    }

    private fun getDateEvents() {
        EventDatabase.getMonthEvents(date.year, date.monthValue) { events ->
            _dateEvents.postValue(events)
        }
        dateEvents.observe(this) { dateEvents ->
            setupEventView(dateEvents)
        }
    }

    private fun setupEventView(dateEvents: List<DateEvent>?) {
        if (dateEvents == null) return;

        val eventRecyclerView = findViewById<RecyclerView>(R.id.rvDateEvents)
        eventRecyclerView.layoutManager = LinearLayoutManager(this)
        eventRecyclerView.adapter =
            EventsRecycleViewAdapter(dateEvents, this@DateActivity) { dateEvent ->
                eventClearClick(dateEvent)
            }
    }

    private fun eventClearClick(dateEvent: DateEvent) {
        EventDatabase.deleteDateEvent(dateEvent.id)
    }

    private fun createEvent() {
        Log.d("DateActivity", "createEvent ")
        val intent = Intent(this@DateActivity, CreateEventActivity::class.java)
        intent.putExtra("date", localDateToEpochSecond(date))
        startActivity(intent)
    }
}