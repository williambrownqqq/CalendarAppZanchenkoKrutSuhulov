package com.ZanchenkoKrutSugulov.calendarapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ZanchenkoKrutSugulov.calendarapp.activities.DateActivity
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.CalendarDay
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.db.DateEvent
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.EventsRecycleViewAdapter
import com.ZanchenkoKrutSugulov.calendarapp.utils.localDateToEpochSecond
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.mainActivity.MainActivityViewModel
import com.ZanchenkoKrutSugulov.calendarapp.viewModels.activities.mainActivity.MainActivityViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.recyclerview.widget.GridLayoutManager
import com.ZanchenkoKrutSugulov.calendarapp.recycleViews.CalendarRecycleViewAdapter


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null
    private var button: Button? = null
    private var textView: TextView? = null
    private var user: FirebaseUser? = null

    private lateinit var activityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance();
        print(auth)
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        user = auth!!.currentUser;
        print(user)
        if (user == null) {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        } else {
            textView!!.text = user!!.email
        }
        button!!.setOnClickListener(View.OnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        })
    }
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Check user authentication status
//        auth = FirebaseAuth.getInstance()
//        user = auth!!.currentUser
//
//        if (user == null) {
//            val intent = Intent(applicationContext, Login::class.java)
//            startActivity(intent)
//            finish()
//        } else {
//            // Proceed with calendar setup and functionalities
//            initializeCalendar()
//        }
//    }
//
//
//    private fun initializeCalendar() {
//        setupActivityViewModel()
//        setupUIComponents()
//        getMonthEvents()
//    }
//
//    private fun setupActivityViewModel() {
//        val factory = MainActivityViewModelFactory(application, this)
//        activityViewModel = ViewModelProvider(this, factory)[MainActivityViewModel::class.java]
//    }
//
//    private fun setupUIComponents() {
//        // Set up UI components for the calendar as per your implementation
//        // Add setupSpinners(), setupCalendarView(), setupEventView(), etc.
//        // ...
//
//        // Example button setup for logging out
//        button = findViewById(R.id.logout)
//        button!!.setOnClickListener {
//            FirebaseAuth.getInstance().signOut()
//            val intent = Intent(applicationContext, Login::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }
//
//
//    private fun getMonthEvents() {
//        activityViewModel.getMonthEvents()
//        observeMonthEvents()
//    }
//
//
//    private fun observeMonthEvents() {
//        activityViewModel.monthEvents.observe(this) { dateEvents ->
//            if (dateEvents != null) {
//                setupCalendarView()
//                setupEventView(dateEvents)
//            }
//        }
//    }
//
//    fun setupCalendarView() {
//        val calendarRecycleView = findViewById<RecyclerView>(R.id.rvCalendar)
//        calendarRecycleView.layoutManager = GridLayoutManager(this, 7)
//        calendarRecycleView.adapter = activityViewModel.monthEvents.value?.let { dateEvents ->
//            CalendarRecycleViewAdapter(activityViewModel.currentDate, { calendarDay ->
//                calendarDayClick(
//                    calendarDay
//                )
//            }, dateEvents,
//                getColorStateList(R.color.white),
//                getColorStateList(R.color.dayWithEvent),
//                getColorStateList(R.color.black),
//                getColorStateList(R.color.currentDayWithEvent)
//            )
//        }
//    }
//
//
//    private fun setupEventView(dateEvents: List<DateEvent>) {
//        val eventRecyclerView = findViewById<RecyclerView>(R.id.rvEvents)
//        eventRecyclerView.layoutManager = LinearLayoutManager(this)
//        eventRecyclerView.adapter = EventsRecycleViewAdapter(dateEvents, this@MainActivity) {dateEvent ->
//            eventClearClick(dateEvent)
//        }
//
//        val monthEventsTextView = findViewById<TextView>(R.id.tvMonthEvents)
//        monthEventsTextView.text = "Events of month (${dateEvents.size})"
//    }
//
//
//    private fun calendarDayClick(calendarDay: CalendarDay) {
//        val intent = Intent(this@MainActivity, DateActivity::class.java)
//        intent.putExtra("date", localDateToEpochSecond(calendarDay.date))
//        startActivity(intent)
//    }
//
//
//    private fun eventClearClick(dateEvent: DateEvent) {
//        activityViewModel.dateEventViewModel.deleteDateEvent(dateEvent)
//    }
}