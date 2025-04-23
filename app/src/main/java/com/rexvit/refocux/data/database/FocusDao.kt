package com.rexvit.refocux.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rexvit.refocux.data.model.FocusSession
import com.rexvit.refocux.data.model.Task
import com.rexvit.refocux.data.model.UserStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface FocusDao {

    @Insert
    suspend fun insertTask(task: Task)

    @Insert
    suspend fun insertSession(session: FocusSession)

    @Update
    suspend fun updateTask(task: Task)

    @Update
    suspend fun updateStats(stats: UserStats)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, priority DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate = :date")
    fun getTasksByDueDate(date: LocalDate): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY dueDate ASC, priority DESC LIMIT 1")
    fun getCurrentTask(): Flow<Task?>

    @Query("SELECT * FROM focus_sessions WHERE date(startTime) = date(:date) ORDER BY startTime DESC")
    fun getSessionsByDate(date: LocalDate): Flow<List<FocusSession>>

    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    fun getUserStats(userId: String): Flow<UserStats?>

    @Query("SELECT * FROM focus_sessions WHERE sessionType = 'POMODORO' AND completed = 1")
    fun getAllCompletedPomodoros(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE date(startTime) BETWEEN date(:startDate) AND date(:endDate)")
    fun getSessionsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<FocusSession>>

}