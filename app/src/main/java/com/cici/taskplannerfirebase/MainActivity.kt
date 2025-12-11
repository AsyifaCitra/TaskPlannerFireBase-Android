package com.cici.taskplannerfirebase

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cici.taskplannerfirebase.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var tasksRef: DatabaseReference
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTasks.layoutManager = LinearLayoutManager(this)
        tasksRef = FirebaseDatabase.getInstance().getReference("tasks")

        fetchData()
        setupAddButton()
    }

    private fun fetchData() {
        tasksRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = mutableListOf<Task>()

                for (data in snapshot.children) {
                    val task = data.getValue(Task::class.java)
                    task?.let {
                        tasks.add(it)
                        Log.d("MainActivity", "Task loaded: ${it.title}, completed: ${it.completed}")
                    }
                }

                val sortedTasks = tasks.sortedWith(
                    compareBy<Task> { it.completed }
                        .thenBy { parseDate(it.deadline) }
                )

                Log.d("MainActivity", "Total tasks: ${sortedTasks.size}")

                if (sortedTasks.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.tvTasks.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.tvTasks.visibility = View.VISIBLE
                }

                binding.tvTasks.adapter = TaskAdapter(
                    sortedTasks,
                    onTaskChecked = { task, isChecked -> updateTaskStatus(task, isChecked) },
                    onTaskClick = { task -> editTask(task) },
                    onTaskDelete = { task -> showDeleteConfirmation(task) }
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Firebase error: ${error.message}")
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun setupAddButton() {
        binding.fabAddTask.setOnClickListener {
            val dialog = CreateTaskDialog(this, tasksRef)
            dialog.show()
        }
    }

    private fun updateTaskStatus(task: Task, isChecked: Boolean) {
        Log.d("MainActivity", "Updating task: ${task.title} to completed: $isChecked")

        val updatedTask = hashMapOf(
            "id" to task.id,
            "title" to task.title,
            "description" to task.description,
            "deadline" to task.deadline,
            "completed" to isChecked
        )

        tasksRef.child(task.id).setValue(updatedTask)
            .addOnSuccessListener {
                Log.d("MainActivity", "Task updated successfully")
                val message = if (isChecked) "Tugas selesai! 🎉" else "Tugas dibuka kembali"
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(
                        resources.getColor(
                            if (isChecked) R.color.Biru else R.color.Pink,
                            null
                        )
                    )
                    .show()
            }
            .addOnFailureListener { error ->
                Log.e("MainActivity", "Failed to update task: ${error.message}")
                Toast.makeText(this, "Gagal memperbarui tugas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editTask(task: Task) {
        val dialog = CreateTaskDialog(this, tasksRef, task)
        dialog.show()
    }

    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Tugas")
            .setMessage("Apakah Anda yakin ingin menghapus tugas \"${task.title}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteTask(task)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteTask(task: Task) {
        tasksRef.child(task.id).removeValue()
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Tugas dihapus", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark, null))
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus tugas", Toast.LENGTH_SHORT).show()
            }
    }
}