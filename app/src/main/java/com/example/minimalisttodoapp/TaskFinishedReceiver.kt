package com.example.minimalisttodoapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.minimalisttodoapp.R

class TaskFinishedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Get task information from the intent (optional)
        val taskName = intent.getStringExtra("TASK_NAME") ?: "Task"

        // Show a notification or toast when the task is finished
        showTaskFinishedNotification(context, taskName)
    }

    // Function to show a notification when the task finishes
    private fun showTaskFinishedNotification(context: Context, taskName: String) {
        val notificationId = 1
        val builder = NotificationCompat.Builder(context, "task_finished_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // Replace with your app icon
            .setContentTitle("Task Finished")
            .setContentText("Task: $taskName is now complete!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)


        val intent = Intent(context, TaskFinishedReceiver::class.java)
        intent.putExtra("TASK_NAME", "Your Task Name")  // Pass task info if needed
        context.sendBroadcast(intent)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }
}
