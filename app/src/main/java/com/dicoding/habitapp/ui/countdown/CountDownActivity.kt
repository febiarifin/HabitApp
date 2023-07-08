package com.dicoding.habitapp.ui.countdown

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIF_UNIQUE_WORK
import java.util.concurrent.TimeUnit

class CountDownActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"

        val habit = intent.getParcelableExtra<Habit>(HABIT) as Habit

        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

        val viewModel = ViewModelProvider(this).get(CountDownViewModel::class.java)

        val countDown = findViewById<TextView>(R.id.tv_count_down)

        //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished
        viewModel.setInitialTime(habit.minutesFocus)
        viewModel.currentTimeString.observe(this, {
            countDown.text = it
        })
        val workManager = WorkManager.getInstance(this)
        val inputDataNotification = Data.Builder()
            .putInt(HABIT_ID, habit.id)
            .putString(HABIT_TITLE, habit.title)
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(inputDataNotification)
            .build()
        viewModel.eventCountDownFinish.observe(this) { countDownFinish ->
            if (countDownFinish) {
                workManager.enqueueUniqueWork(
                    NOTIF_UNIQUE_WORK,
                    ExistingWorkPolicy.REPLACE,
                    oneTimeWorkRequest
                )
                updateButtonState(false)
            }
        }

        //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.
        viewModel.eventCountDownFinish.observe(this, {
            updateButtonState(!it)
        })

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            val habit = workDataOf(HABIT_ID to habit.id, HABIT_TITLE to habit.title)
            val initialTime = viewModel.getInitialTime()
            viewModel.startTimer()
            updateButtonState(true)

            val notifyWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInputData(habit)
                    .setInitialDelay(initialTime!!, TimeUnit.MILLISECONDS).build()
            WorkManager
                .getInstance(this)
                .enqueue(notifyWorkRequest)
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            viewModel.resetTimer()
            updateButtonState(false)
            WorkManager.getInstance(this).cancelAllWork()
        }
    }

    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }
}