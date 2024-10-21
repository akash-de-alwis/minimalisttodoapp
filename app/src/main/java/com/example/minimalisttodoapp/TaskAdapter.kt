package com.example.minimalisttodoapp.adapters

import android.content.Context
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.minimalisttodoapp.R
import com.example.minimalisttodoapp.databinding.ItemTaskBinding
import com.example.minimalisttodoapp.models.Task
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class TaskAdapter(
    private val context: Context,
    private val tasks: MutableList<Task>,
    private val onItemClick: (Task, Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var countdownTimers: MutableMap<Int, CountDownTimer> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
        holder.itemView.setOnClickListener {
            onItemClick(task, position)
        }
        holder.binding.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount() = tasks.size

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTaskName.text = task.name
            binding.tvTaskDescription.text = task.description
            binding.tvTaskTime.text = task.time
            binding.tvTaskDate.text = task.date

            // Set priority
            binding.tvTaskPriority.text = when (task.priority) {
                0 -> "Low"
                1 -> "Medium"
                else -> "High"
            }

            // Calculate countdown
            startCountdown(task.date, task.time, adapterPosition)
        }

        private fun startCountdown(date: String, time: String, position: Int) {
            countdownTimers[position]?.cancel() // Cancel any previous timer for this position

            val calendar = Calendar.getInstance()
            val currentTimeMillis = calendar.timeInMillis

            val dateTimeString = "$date $time"
            val dateFormat = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
            val targetTimeMillis = try {
                dateFormat.parse(dateTimeString)?.time ?: currentTimeMillis
            } catch (e: Exception) {
                currentTimeMillis
            }

            val remainingTimeMillis = (targetTimeMillis - currentTimeMillis).absoluteValue

            if (remainingTimeMillis > 0) {
                val countdownTimer = object : CountDownTimer(remainingTimeMillis, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val seconds = (millisUntilFinished / 1000) % 60
                        val minutes = (millisUntilFinished / (1000 * 60)) % 60
                        val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                        val days = (millisUntilFinished / (1000 * 60 * 60 * 24))
                        binding.countdown.text = String.format("%d days %02d:%02d:%02d", days, hours, minutes, seconds)
                    }

                    override fun onFinish() {
                        binding.countdown.text = "Time's up!"
                        playNotificationTone()
                        showCompletionDialog()
                    }
                }.start()

                countdownTimers[position] = countdownTimer
            } else {
                binding.countdown.text = "Time's up!"
                playNotificationTone()
                showCompletionDialog()
            }
        }

        private fun playNotificationTone() {
            val mediaPlayer = MediaPlayer.create(context, R.raw.alarm_tone)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
        }

        private fun showCompletionDialog() {
            AlertDialog.Builder(context)
                .setTitle("Task Completed")
                .setMessage("The task is finished!")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onViewRecycled(holder: TaskViewHolder) {
        super.onViewRecycled(holder)
        // Cancel the countdown timer for the recycled view holder's position
        countdownTimers[holder.adapterPosition]?.cancel()
        countdownTimers.remove(holder.adapterPosition)
    }
}
