package com.example.happypet

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import java.util.*

class ScheduleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        val statusPageButton = findViewById<Button>(R.id.status_page_button)
        statusPageButton.setOnClickListener {
            val intent = Intent(this, StatusPageActivity::class.java)
            startActivity(intent)
        }

        // Check and request exact alarm permission
        checkAndRequestExactAlarmPermission()

        val addFeedingTimeButton = findViewById<Button>(R.id.add_feeding_time_button)
        val feedingTimesList = findViewById<LinearLayout>(R.id.feeding_times_list)

        val addVetReminderButton = findViewById<Button>(R.id.add_vet_reminder_button)
        val vetRemindersList = findViewById<LinearLayout>(R.id.vet_reminders_list)

        createNotificationChannel()
        checkAndRequestNotificationPermission()

        // Load saved schedules
        val savedFeedingTimes = loadSchedulesFromPreferences("feedingSchedules")
        savedFeedingTimes.forEach { time -> addScheduleToList(time, feedingTimesList, "Feeding Time") }

        val savedVetReminders = loadSchedulesFromPreferences("vetSchedules")
        savedVetReminders.forEach { time -> addScheduleToList(time, vetRemindersList, "Vet Visit") }

        // Add new feeding time
        addFeedingTimeButton.setOnClickListener {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                addScheduleToList(time, feedingTimesList, "Feeding Time")
                saveSchedulesToPreferences("feedingSchedules", feedingTimesList)

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // Ensure time is in the future
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
                }

                scheduleNotification("Time to feed your pet!", calendar.timeInMillis)
            }, currentHour, currentMinute, true)

            timePicker.show()
        }

        // Add new vet reminder
        addVetReminderButton.setOnClickListener {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                TimePickerDialog(this, { _, hourOfDay, minute ->
                    val reminder = String.format(
                        "%04d-%02d-%02d %02d:%02d",
                        year, month + 1, dayOfMonth, hourOfDay, minute
                    )
                    addScheduleToList(reminder, vetRemindersList, "Vet Visit")
                    saveSchedulesToPreferences("vetSchedules", vetRemindersList)

                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    // Ensure time is in the future
                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        println("Error: Cannot schedule a vet reminder for a past time.")
                        return@TimePickerDialog
                    }

                    scheduleNotification("Vet visit reminder!", calendar.timeInMillis)
                }, 12, 0, true).show()
            }, currentYear, currentMonth, currentDay).show()
        }

        // Test notification button
        findViewById<Button>(R.id.test_notification_button).setOnClickListener {
            val intent = Intent(this, NotificationReceiver::class.java)
            intent.putExtra("NOTIFICATION_MESSAGE", "Test Notification")
            sendBroadcast(intent)
        }
    }

    private fun addScheduleToList(time: String, list: LinearLayout, label: String) {
        val scheduleLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val textView = TextView(this).apply {
            text = "$label: $time"
            textSize = 16f
        }

        val editButton = Button(this).apply {
            text = "Edit"
            setOnClickListener {
                if (label == "Feeding Time") {
                    val parts = time.split(":").map { it.toInt() }
                    editFeedingTime(textView, parts[0], parts[1])
                } else {
                    val dateParts = time.split(" ")[0].split("-").map { it.toInt() }
                    val timeParts = time.split(" ")[1].split(":").map { it.toInt() }
                    editVetReminder(textView, dateParts[0], dateParts[1] - 1, dateParts[2], timeParts[0], timeParts[1])
                }
            }
        }

        val deleteButton = Button(this).apply {
            text = "Delete"
            setOnClickListener {
                list.removeView(scheduleLayout)
                if (label == "Feeding Time") {
                    saveSchedulesToPreferences("feedingSchedules", list)
                } else {
                    saveSchedulesToPreferences("vetSchedules", list)
                }
            }
        }

        scheduleLayout.addView(textView)
        scheduleLayout.addView(editButton)
        scheduleLayout.addView(deleteButton)
        list.addView(scheduleLayout)
    }

    private fun scheduleNotification(message: String, timeInMillis: Long) {
        try {
            val currentTime = System.currentTimeMillis()

            // Debugging: Check if time is valid
            if (timeInMillis <= currentTime) {
                println("Error: Cannot schedule notification for a past time.")
                return
            }

            val intent = Intent(this, NotificationReceiver::class.java).apply {
                putExtra("NOTIFICATION_MESSAGE", message)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }

            // Debugging log
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            println("Alarm scheduled: $message at ${calendar.time}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "HappyPetChannel",
                "HappyPet Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for pet reminders"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun saveSchedulesToPreferences(key: String, list: LinearLayout) {
        val schedules = list.children.map { (it as LinearLayout).getChildAt(0) as TextView }
            .map { it.text.toString().substringAfter(": ").trim() }
            .toSet()
        val sharedPreferences = getSharedPreferences("HappyPetSchedules", Context.MODE_PRIVATE)
        sharedPreferences.edit().putStringSet(key, schedules).apply()
    }

    private fun loadSchedulesFromPreferences(key: String): List<String> {
        val sharedPreferences = getSharedPreferences("HappyPetSchedules", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet(key, emptySet())?.toList() ?: emptyList()
    }

    private fun editFeedingTime(textView: TextView, oldHour: Int, oldMinute: Int) {
        val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
            val newTime = String.format("%02d:%02d", hourOfDay, minute)
            textView.text = "Feeding Time: $newTime"
            saveSchedulesToPreferences("feedingSchedules", findViewById(R.id.feeding_times_list))
        }, oldHour, oldMinute, true)
        timePicker.show()
    }

    private fun editVetReminder(
        textView: TextView,
        year: Int,
        month: Int,
        day: Int,
        oldHour: Int,
        oldMinute: Int
    ) {
        DatePickerDialog(this, { _, newYear, newMonth, newDay ->
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val newReminder = String.format("%04d-%02d-%02d %02d:%02d", newYear, newMonth + 1, newDay, hourOfDay, minute)
                textView.text = "Vet Visit: $newReminder"
                saveSchedulesToPreferences("vetSchedules", findViewById(R.id.vet_reminders_list))
            }, oldHour, oldMinute, true).show()
        }, year, month, day).show()
    }
}
