package com.ZanchenkoKrutSugulov.calendarapp;
import java.util.*
import android.app.Activity
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.ZanchenkoKrutSugulov.calendarapp.R

class NotificationReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
                val message = intent.getStringExtra("message")
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val notification = Notification.Builder(context, "event_channel")
                    .setContentTitle("Нагадування про подію")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .build()

                notificationManager.notify(200, notification)

                Log.d("showNotification", "showNotification: " + notification.toString());

            }
        }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
