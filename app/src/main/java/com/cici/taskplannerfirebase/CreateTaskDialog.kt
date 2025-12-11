package com.cici.taskplannerfirebase

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.cici.taskplannerfirebase.databinding.DialogCreateTaskBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CreateTaskDialog(
    private val context: Context,
    private val tasksRef: DatabaseReference,
    private val task: Task? = null
) {

    fun show() {
        val dialogBinding = DialogCreateTaskBinding.inflate(LayoutInflater.from(context))

        task?.let {
            dialogBinding.editTextTitle.setText(it.title)
            dialogBinding.editTextDescription.setText(it.description)
            dialogBinding.editTextDeadline.setText(it.deadline)
        }

        dialogBinding.editTextDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dialogBinding.editTextDeadline.setText(dateFormat.format(selectedCalendar.time))
                },
                year, month, day
            )

            datePickerDialog.datePicker.minDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        val dialogTitle = if (task == null) "Tambah Tugas Baru" else "Edit Tugas"
        val buttonText = if (task == null) "Simpan" else "Simpan"

        MaterialAlertDialogBuilder(context)
            .setTitle(dialogTitle)
            .setView(dialogBinding.root)
            .setPositiveButton(buttonText) { dialog, _ ->
                val title = dialogBinding.editTextTitle.text.toString().trim()
                val description = dialogBinding.editTextDescription.text.toString().trim()
                val deadline = dialogBinding.editTextDeadline.text.toString().trim()

                if (title.isEmpty() || deadline.isEmpty()) {
                    Toast.makeText(context, "Judul dan Deadline wajib diisi!", Toast.LENGTH_SHORT).show()
                } else {
                    if (task == null) {
                        saveTaskToFirebase(title, description, deadline)
                    } else {
                        updateTaskInFirebase(task.id, title, description, deadline, task.completed)
                    }
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveTaskToFirebase(title: String, description: String, deadline: String) {
        val id = tasksRef.push().key ?: return
        val newTask = Task(
            id = id,
            title = title,
            description = description,
            deadline = deadline,
            completed = false
        )

        tasksRef.child(id).setValue(newTask)
            .addOnSuccessListener {
                Toast.makeText(context, "Tugas berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTaskInFirebase(id: String, title: String, description: String, deadline: String, isCompleted: Boolean) {
        val updatedTask = Task(
            id = id,
            title = title,
            description = description,
            deadline = deadline,
            completed = isCompleted
        )

        tasksRef.child(id).setValue(updatedTask)
            .addOnSuccessListener {
                Toast.makeText(context, "Tugas berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}