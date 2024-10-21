package com.example.minimalisttodoapp.widgets

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.minimalisttodoapp.MainActivity
import com.example.minimalisttodoapp.R
import com.example.minimalisttodoapp.models.Task

class UpcomingTasksWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Loop through all widget instances
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        // Dummy method to get upcoming tasks (You will replace this with actual task fetching logic)
        private fun getUpcomingTasks(context: Context): List<Task> {
            // Fetch upcoming tasks logic (use SharedPreferences, Room database, etc.)
            // For now, we will return mock tasks
            return listOf(
                Task("Task 1", "Complete this", "10:00", 1, "19/09/2024"),
                Task("Task 2", "Finish this", "15:00", 2, "19/09/2024"),
                Task("Task 3", "Do this", "12:00", 0, "20/09/2024")
            )
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val widgetText = "Upcoming Tasks"

            // Get the layout for the widget and attach tasks to the TextViews
            val views = RemoteViews(context.packageName, R.layout.widget_upcoming_tasks)

            // Set widget title
            views.setTextViewText(R.id.tvWidgetTitle, widgetText)

            // Get upcoming tasks (replace this logic with actual task fetching)
            val upcomingTasks = getUpcomingTasks(context)

            // Update task texts
            if (upcomingTasks.isNotEmpty()) {
                views.setTextViewText(R.id.tvWidgetTask1, upcomingTasks[0].name)
            }

            // Create intent to launch MainActivity when widget is clicked
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.tvWidgetTitle, pendingIntent)

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
