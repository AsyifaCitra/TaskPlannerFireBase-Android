package com.cici.taskplannerfirebase

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cici.taskplannerfirebase.databinding.ItemTaskBinding


class TaskAdapter(
    private val tasks: List<Task>,
    private val onTaskChecked: (Task, Boolean) -> Unit,
    private val onTaskClick: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            task: Task,
            onTaskChecked: (Task, Boolean) -> Unit,
            onTaskClick: (Task) -> Unit,
            onTaskDelete: (Task) -> Unit
        ) {
            Log.d("TaskAdapter", "Binding task: ${task.title}, completed: ${task.completed}")

            binding.tvTitle.text = task.title
            binding.tvDescription.text = if (task.description.isEmpty()) "" else task.description
            binding.tvDescription.visibility = if (task.description.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            binding.tvDeadline.text = task.deadline

            updateTaskStyle(task.completed)

            binding.checkboxFrame.setOnClickListener {
                val newStatus = !task.completed
                Log.d("TaskAdapter", "Checkbox clicked! Old: ${task.completed}, New: $newStatus")
                onTaskChecked(task, newStatus)
            }

            binding.contentLayout.setOnClickListener {
                Log.d("TaskAdapter", "Content clicked for: ${task.title}")
                onTaskClick(task)
            }

            binding.btnDelete.setOnClickListener {
                Log.d("TaskAdapter", "Delete clicked for: ${task.title}")
                onTaskDelete(task)
            }
        }

        private fun updateTaskStyle(isCompleted: Boolean) {
            Log.d("TaskAdapter", "Updating style, isCompleted: $isCompleted")
            if (isCompleted) {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTitle.alpha = 0.5f
                binding.tvDescription.alpha = 0.5f
                binding.tvDeadline.alpha = 0.5f
                binding.cardTask.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
                binding.checkboxCustom.setBackgroundResource(R.drawable.checkbox_checked)
            } else {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTitle.alpha = 1f
                binding.tvDescription.alpha = 1f
                binding.tvDeadline.alpha = 1f
                binding.cardTask.setCardBackgroundColor(Color.WHITE)
                binding.checkboxCustom.setBackgroundResource(R.drawable.checkbox_unchecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position], onTaskChecked, onTaskClick, onTaskDelete)
    }

    override fun getItemCount() = tasks.size
}