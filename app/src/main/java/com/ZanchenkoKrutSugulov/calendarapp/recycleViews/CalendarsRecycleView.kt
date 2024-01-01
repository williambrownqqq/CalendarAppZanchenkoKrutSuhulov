package com.ZanchenkoKrutSugulov.calendarapp.recycleViews

import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ZanchenkoKrutSugulov.calendarapp.R


class CalendarAdapter(
    private val calendars: List<Calendar>,
    private val onEdit: (Calendar) -> Unit,
    private val onDelete: (Calendar) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvCalendarName: TextView = view.findViewById(R.id.tvCalendarName)
        private val btnEdit: Button = view.findViewById(R.id.btnEdit)
        private val btnDelete: Button = view.findViewById(R.id.btnDelete)

        fun bind(calendar: Calendar, onEdit: (Calendar) -> Unit, onDelete: (Calendar) -> Unit, isPrimary: Boolean, isOnlyCalendar: Boolean)  {
            tvCalendarName.text = calendar.name
            btnEdit.setOnClickListener { onEdit(calendar) }
            btnDelete.isEnabled = !(isPrimary || isOnlyCalendar)
            btnDelete.alpha = if (btnDelete.isEnabled) 1.0f else 0.5f
            if (btnDelete.isEnabled) {
                btnDelete.setOnClickListener { onDelete(calendar) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        return CalendarViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_calendar, parent, false))
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val calendar = calendars[position]
        holder.bind(calendar, onEdit, onDelete, calendar.primary, calendars.size == 1)
    }

    override fun getItemCount() = calendars.size
}
