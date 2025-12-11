package com.cici.taskplannerfirebase

data class Task(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var deadline: String = "",
    var completed: Boolean = false,
    var timestamp: Long = System.currentTimeMillis()
)
