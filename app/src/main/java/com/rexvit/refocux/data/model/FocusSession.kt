package com.rexvit.refocux.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val durationMinutes: Int,
    val sessionType: SessionType,
    val completed: Boolean
)

enum class SessionType {
    POMODORO, SHORT_BREAK, LONG_BREAK
}