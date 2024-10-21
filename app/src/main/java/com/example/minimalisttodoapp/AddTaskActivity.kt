package com.example.minimalisttodoapp

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.minimalisttodoapp.databinding.ActivityAddTaskBinding
import com.example.minimalisttodoapp.models.Task
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private var editMode = false
    private var taskPosition = -1
    private lateinit var binding: ActivityAddTaskBinding
    private val CHANNEL_ID = "TASK_NOTIFICATION_CHANNEL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val task = intent.getSerializableExtra("task") as Task?
        taskPosition = intent.getIntExtra("position", -1)

        if (task != null) {
            editMode = true
            fillTaskDetails(task)
        }

        binding.btnSelectDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnSelectTime.setOnClickListener {
            showTimePickerDialog()
        }

        binding.btnSaveTask.setOnClickListener {
            val name = binding.etTaskName.text.toString()
            val description = binding.etTaskDescription.text.toString()
            val time = binding.tvSelectedTime.text.toString().replace("Selected Time: ", "")
            val priority = binding.spinner.selectedItemPosition
            val date = binding.tvSelectedDate.text.toString().replace("Selected Date: ", "")

            if (name.isBlank()) {
                Toast.makeText(this, "Task name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newTask = Task(name, description, time, priority, date)
            saveTask(newTask)
            finish()
        }

        createNotificationChannel()
    }

    private fun fillTaskDetails(task: Task) {
        binding.etTaskName.setText(task.name)
        binding.etTaskDescription.setText(task.description)
        binding.tvSelectedTime.text = "Selected Time: ${task.time}"
        binding.tvSelectedDate.text = "Selected Date: ${task.date}"
        binding.spinner.setSelection(task.priority)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.tvSelectedDate.text = "Selected Date: $date"
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.tvSelectedTime.text = "Selected Time: $time"
            },
            hour, minute, true
        )
        timePickerDialog.show()
    }

    private fun showNotification() {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_save) // Replace with your notification icon
            .setContentTitle("Task Finished")
            .setContentText("Your task has been completed!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Task Notification Channel"
            val descriptionText = "Channel for task notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun saveTask(task: Task) {
        val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val taskCount = sharedPreferences.getInt("task_count", 0)

        if (editMode && taskPosition >= 0) {
            editor.putString("task_name_$taskPosition", task.name)
            editor.putString("task_description_$taskPosition", task.description)
            editor.putString("task_time_$taskPosition", task.time)
            editor.putString("task_date_$taskPosition", task.date)
            editor.putInt("task_priority_$taskPosition", task.priority)
        } else {
            editor.putString("task_name_$taskCount", task.name)
            editor.putString("task_description_$taskCount", task.description)
            editor.putString("task_time_$taskCount", task.time)
            editor.putString("task_date_$taskCount", task.date)
            editor.putInt("task_priority_$taskCount", task.priority)
            editor.putInt("task_count", taskCount + 1)
        }

        editor.apply()
    }
}
