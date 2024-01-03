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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.EventsRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.utils.epochSecondToLocalDate
import com.ZanchenkoKrutSugulov.calendarapp.utils.getDaysOfWeekArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.getMonthsArray
import com.ZanchenkoKrutSugulov.calendarapp.utils.localDateToEpochSecond
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.DateActivityViewModel
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
class DateActivity : AppCompatActivity() {
    private lateinit var date: ZonedDateTime
    private lateinit var dateActivityViewModel: DateActivityViewModel

    private lateinit var backButton: ImageView
    private val _dateEvents = MutableLiveData<List<DateEvent>>()
    val dateEvents: LiveData<List<DateEvent>> = _dateEvents


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)
        Log.d("DateActivity", "!Create Event calendarDayClick")

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

    //    private fun getIntentExtras() {
//        Log.d("DateActivity", intent.toString())
//        if (intent == null) return;
//        val epochSecond = intent.getLongExtra("date", 0)
//
//        date = epochSecondToLocalDate(epochSecond)
//    }
    private fun getIntentExtras(): Boolean {
        val epochSecond = intent.getLongExtra("date", 0)
        Log.d("getIntentExtras",  "!Create Event calendarDayClick $epochSecond")
        return if (epochSecond != 0L) {
            date = epochSecondToLocalDate(epochSecond)
            Log.d("getIntentExtras",  "!Create Event calendarDayClick $date")

            true
        } else {
            false
        }
    }

//    private fun setupActivityViewModel() {
//        Log.d("DateActivity",  "!Create Event calendarDayClick setupActivityViewModel")
//        dateActivityViewModel = ViewModelProvider(this)[DateActivityViewModel::class.java]
//        observeDateEvents()
//        observeMonthEvents()
//    }
private fun setupActivityViewModel() {
    Log.d("DateActivity",  "!Create Event calendarDayClick setupActivityViewModel")
    dateActivityViewModel = ViewModelProvider(this)[DateActivityViewModel::class.java]
    observeDateEvents()
    observeMonthEvents()
}
    private fun observeDateEvents() {
        Log.d("DateActivity",  "!Create Event calendarDayClick observeDateEvents")

        dateActivityViewModel.dateEvents.observe(this) { dateEvents ->
            setupEventView(dateEvents)
        }
        dateActivityViewModel.getDateEvents(date)
    }

    private fun observeMonthEvents() {
        dateActivityViewModel.dateEvents.observe(this) { dateEvents ->
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

    private fun setupButtons() {
        val createEventButton = findViewById<Button>(R.id.btnCreateEvent)
        createEventButton.setOnClickListener {
            createEvent()
        }
    }

    private fun getDateEvents() {
        dateActivityViewModel.getMonthEvents()
        dateActivityViewModel.dateEvents.observe(this) { dateEvents ->
            setupEventView(dateEvents)
        }
    }

    private fun setupEventView(dateEvents: List<DateEvent>?) {

        Log.d("DateActivity",  "!Create Event calendarDayClick setupEventView dateEvents: ${dateEvents?.map { it }}")

        if (dateEvents == null) return;

        val eventRecyclerView = findViewById<RecyclerView>(R.id.rvDateEvents)
        eventRecyclerView.layoutManager = LinearLayoutManager(this)
        eventRecyclerView.adapter =
            EventsRecycleViewAdapter(dateEvents, this@DateActivity) { dateEvent ->
                eventClearClick(dateEvent)
            }
    }

    private fun eventClearClick(dateEvent: DateEvent) {
        dateActivityViewModel.deleteDateEvent(dateEvent)
    }

    private fun createEvent() {
        val intent = Intent(this@DateActivity, CreateEventActivity::class.java)
        intent.putExtra("date", localDateToEpochSecond(date))
        startActivity(intent)
    }
}