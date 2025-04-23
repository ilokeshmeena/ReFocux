package com.rexvit.refocux.data.repository

import com.rexvit.refocux.data.database.FocusDao
import com.rexvit.refocux.data.model.Badge
import com.rexvit.refocux.data.model.FocusSession
import com.rexvit.refocux.data.model.Task
import com.rexvit.refocux.data.model.UserStats
import com.rexvit.refocux.data.model.allBadges
import com.rexvit.refocux.service.FirebaseService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject


class FocusRepository @Inject constructor(
    private val focusDao: FocusDao,
    private val firebaseService: FirebaseService
) {

    suspend fun insertSession(session: FocusSession) {
        focusDao.insertSession(session)
    }

    suspend fun updateStats(stats: UserStats) {
        focusDao.updateStats(stats)
    }

    fun getSessionsByDate(date: LocalDate): Flow<List<FocusSession>> {
        return focusDao.getSessionsByDate(date)
    }

    fun getUserStats(userId: String): Flow<UserStats?> {
        return focusDao.getUserStats(userId)
    }

    fun getAllCompletedPomodoros(): Flow<List<FocusSession>> {
        return focusDao.getAllCompletedPomodoros()
    }

    fun getSessionsBetweenDates(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<FocusSession>> {
        return focusDao.getSessionsBetweenDates(startDate, endDate)
    }

    // Task management
    suspend fun insertTask(task: Task) {
        focusDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        focusDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        focusDao.deleteTask(task)
    }

    fun getAllTasks(): Flow<List<Task>> {
        return focusDao.getAllTasks()
    }

    fun getCurrentTask(): Flow<Task?> {
        return focusDao.getCurrentTask()
    }

    fun getTasksByDueDate(date: LocalDate): Flow<List<Task>> {
        return focusDao.getTasksByDueDate(date)
    }

    // Badge system
    fun checkForNewBadges(stats: UserStats): List<Badge> {
        return allBadges.filter { badge ->
            badge.unlockCondition(stats) && !stats.unlockedBadges.contains(badge.id)
        }
    }

    // Firebase sync
    suspend fun syncWithFirebase(userId: String) {

        focusDao.getUserStats(userId).collect { stats ->
            stats?.let { // Only proceed if stats is not null
                firebaseService.syncUserData(userId, it)
            }
        }

        // Sync tasks if needed
        // firebaseService.syncTasks(userId, focusDao.getAllTasks())
    }

    suspend fun fetchFromFirebase(userId: String) {
        val remoteStats = firebaseService.getUserData(userId)
        remoteStats?.let { focusDao.updateStats(it) }
    }

    fun getCurrentUserId(): String {
        return firebaseService.auth.currentUser?.uid ?: run {
            // If not authenticated, use local default user
            "default_user"
        }
    }


}