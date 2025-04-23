package com.rexvit.refocux.service

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsService @Inject constructor() {
    private val analytics: FirebaseAnalytics = Firebase.analytics

    fun logTimerStarted(sessionType: String, duration: Int) {
        analytics.logEvent("timer_started") {
            param("session_type", sessionType)
            param("duration", duration.toLong())
        }
    }

    fun logTimerCompleted(sessionType: String, completed: Boolean) {
        analytics.logEvent("timer_completed") {
            param("session_type", sessionType)
            param("completed", if (completed) 1L else 0L)
        }
    }

    fun logTaskCreated() {
        analytics.logEvent("task_created", null)
    }

    fun logTaskCompleted() {
        analytics.logEvent("task_completed", null)
    }

    fun logBadgeUnlocked(badgeId: String) {
        analytics.logEvent("badge_unlocked") {
            param("badge_id", badgeId)
        }
    }

    fun logLevelUp(level: Int) {
        analytics.logEvent("level_up") {
            param("new_level", level.toLong())
        }
    }
}