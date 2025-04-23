package com.rexvit.refocux.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDate? = null,
    val priority: Priority = Priority.MEDIUM,
    val completed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val estimatedPomodoros: Int = 1,
    val completedPomodoros: Int = 0
)

enum class Priority {
    LOW, MEDIUM, HIGH
}