package com.rexvit.refocux.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val userId: String = "default_user",
    val totalPomodoros: Int = 0,
    val totalFocusMinutes: Long = 0,
    val currentStreak: Int = 0,
    val level: Int = 1,
    val experience: Int = 0,
    val lastSessionDate: String? = null,
    val unlockedBadges: List<String> = emptyList()
)