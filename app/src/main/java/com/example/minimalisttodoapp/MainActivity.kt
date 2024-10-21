package com.example.minimalisttodoapp

import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minimalisttodoapp.adapters.TaskAdapter
import com.example.minimalisttodoapp.models.Task
import com.example.minimalisttodoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val taskList = mutableListOf<Task>()
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var binding: ActivityMainBinding
    private val NOTIFICATION_ID = 1
    private var currentTaskTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskAdapter = TaskAdapter(
            this,
            taskList,
            onItemClick = { task, position ->
                val intent = Intent(this, AddTaskActivity::class.java)
                intent.putExtra("task", task)
                intent.putExtra("position", position)
                startActivity(intent)
            },
            onDeleteClick = { position ->
                showDeleteConfirmation(position)
            }
        )

        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTasks.adapter = taskAdapter

        binding.fabAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        // Load tasks and start countdowns
        loadTasksFromPreferences()
        startCountdownsForTasks()
    }

    override fun onResume() {
        super.onResume()
        // Refresh tasks and countdowns when the app resumes
        loadTasksFromPreferences()
        startCountdownsForTasks()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release media player resources
        mediaPlayer?.release()
        mediaPlayer = null
        currentTaskTimer?.cancel()  // Cancel the current countdown timer when the activity is destroyed
        cancelAllCountdownTimers()  // Cancel all active countdown timers
    }

    private fun loadTasksFromPreferences() {
        val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
        taskList.clear()
        val taskCount = sharedPreferences.getInt("task_count", 0)
        for (i in 0 until taskCount) {
            val name = sharedPreferences.getString("task_name_$i", "") ?: ""
            val description = sharedPreferences.getString("task_description_$i", "") ?: ""
            val time = sharedPreferences.getString("task_time_$i", "") ?: ""
            val date = sharedPreferences.getString("task_date_$i", "") ?: ""
            val priority = sharedPreferences.getInt("task_priority_$i", 0)
            taskList.add(Task(name, description, time, priority, date))
        }
        taskAdapter.notifyDataSetChanged()
    }

    private fun deleteTask(position: Int) {
        val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Remove task data
        for (i in position until taskList.size - 1) {
            editor.putString("task_name_$i", sharedPreferences.getString("task_name_${i + 1}", ""))
            editor.putString("task_description_$i", sharedPreferences.getString("task_description_${i + 1}", ""))
            editor.putString("task_time_$i", sharedPreferences.getString("task_time_${i + 1}", ""))
            editor.putString("task_date_$i", sharedPreferences.getString("task_date_${i + 1}", ""))
            editor.putInt("task_priority_$i", sharedPreferences.getInt("task_priority_${i + 1}", 0))
        }
        editor.putInt("task_count", taskList.size - 1)
        editor.apply()

        // Remove task from list
        taskList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)
    }

    private fun showDeleteConfirmation(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes") { _, _ ->
                deleteTask(position)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun startCountdownsForTasks() {
        for (task in taskList) {
            val remainingTime = loadCountdownState(task)
            if (remainingTime > 0) {
                if (remainingTime < System.currentTimeMillis()) {
                    // Timer has expired
                    onCountdownFinished(task)
                    clearCountdownState(task)
                    binding.textViewCountdown.text = "Task finished!"
                } else {
                    // Start or resume the countdown
                    startTaskCountdown(task, remainingTime)
                }
            } else {
                clearCountdownState(task) // Clear countdown if it is expired
            }
        }
    }

    private fun startTaskCountdown(task: Task, durationInMillis: Long) {
        currentTaskTimer?.cancel()

        currentTaskTimer = object : CountDownTimer(durationInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.textViewCountdown.text = "$secondsLeft seconds remaining"
                saveCountdownState(task, millisUntilFinished)
            }

            override fun onFinish() {
                onCountdownFinished(task)
                clearCountdownState(task)
                binding.textViewCountdown.text = "Task finished!"
            }
        }.start()
    }

    private fun onCountdownFinished(task: Task) {
        triggerNotification(task.name)
        if (taskList.isNotEmpty()) { // Check if there are any tasks
            playCompletionTone()
        } else {
            Toast.makeText(this, "No tasks to play tone for.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerNotification(taskName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, "TASK_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_save)  // Replace with your app icon or task icon
            .setContentTitle("Task Completed")
            .setContentText("The task \"$taskName\" is finished!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun playCompletionTone() {
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_tone) // Add your sound file in res/raw
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            it.release() // Release the media player after the sound is played
        }
    }

    private fun loadCountdownState(task: Task): Long {
        val sharedPreferences = getSharedPreferences("countdowns", MODE_PRIVATE)
        return sharedPreferences.getLong(task.name, 0)
    }

    private fun saveCountdownState(task: Task, millisUntilFinished: Long) {
        val sharedPreferences = getSharedPreferences("countdowns", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong(task.name, millisUntilFinished)
        editor.apply()
    }

    private fun clearCountdownState(task: Task) {
        val sharedPreferences = getSharedPreferences("countdowns", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(task.name)
        editor.apply()
    }

    private fun cancelAllCountdownTimers() {
        val sharedPreferences = getSharedPreferences("countdowns", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
