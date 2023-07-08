package com.dicoding.habitapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.habitapp.R
import com.dicoding.habitapp.ui.detail.DetailHabitActivity
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIFICATION_CHANNEL_ID

class NotificationWorker(applicationContext: Context, params: WorkerParameters) : Worker(applicationContext, params) {

    private val habitId = inputData.getInt(HABIT_ID, 0)
    private val habitTitle = inputData.getString(HABIT_TITLE)

    override fun doWork(): Result {
        val prefManager = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val shouldNotify = prefManager.getBoolean(applicationContext.getString(R.string.pref_key_notify), false)

        //TODO 12 : If notification preference on, show notification with pending intent
        if (shouldNotify ) {
            if (habitTitle != null) {
                val detailIntent = Intent(applicationContext, DetailHabitActivity::class.java).apply {
                    putExtra(HABIT_ID, habitId)
                }

                val pendingIntent = TaskStackBuilder.create(applicationContext).run {
                    addNextIntentWithParentStack(detailIntent)
                    getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                }

                val notificationManagerCompat =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(habitTitle)
                    .setContentText(applicationContext.getString(R.string.notify_content))
                    .setAutoCancel(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        "habitNotification",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    builder.setChannelId(NOTIFICATION_CHANNEL_ID)
                    notificationManagerCompat.createNotificationChannel(channel)
                }

                val notification = builder.build()
                notificationManagerCompat.notify(100, notification)
            }
        }

        return Result.success()
    }

}
