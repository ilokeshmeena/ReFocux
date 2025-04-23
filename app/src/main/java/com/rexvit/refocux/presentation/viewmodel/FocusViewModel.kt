package com.rexvit.refocux.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rexvit.refocux.data.model.Badge
import com.rexvit.refocux.data.model.FocusSession
import com.rexvit.refocux.data.model.SessionType
import com.rexvit.refocux.data.model.Task
import com.rexvit.refocux.data.model.UserStats
import com.rexvit.refocux.data.model.allBadges
import com.rexvit.refocux.data.repository.FocusRepository
import com.rexvit.refocux.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class FocusViewModel @Inject constructor(
    private val repository: FocusRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    private val userId = "default_user"

    // Mutable backing property (not exposed directly)
    private val _newBadgesUnlocked = MutableStateFlow<List<Badge>>(emptyList())

    // Public read-only state flow
    val newBadgesUnlocked: StateFlow<List<Badge>> get() = _newBadgesUnlocked

    // Timer state
    var timerState by mutableStateOf(TimerState.IDLE)
        private set

    // Tasks
    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask1: StateFlow<Task?> = _currentTask.asStateFlow()

    var currentSessionType by mutableStateOf(SessionType.POMODORO)
        private set

    var timeRemaining by mutableStateOf(25 * 60) // 25 minutes in seconds
        private set

    var pomodoroDuration by mutableStateOf(25)
        private set

    var shortBreakDuration by mutableStateOf(5)
        private set

    var longBreakDuration by mutableStateOf(15)
        private set

    var pomodorosUntilLongBreak by mutableStateOf(4)
        private set

    var completedPomodorosInSet by mutableStateOf(0)
        private set

    val formattedTime: String
        get() {
            val minutes = timeRemaining.toLong() / 60
            val seconds = timeRemaining.toLong() % 60
            return String.format("%02d:%02d", minutes, seconds)
        }


    // User stats
    val userStats = repository.getUserStats(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    // Today's sessions
    private val _todaySessions = mutableStateOf<List<FocusSession>>(emptyList())

    val todaySessions = repository.getSessionsByDate(LocalDate.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Load today's sessions
            repository.getSessionsByDate(LocalDate.now()).collect { sessions ->
                _todaySessions.value = sessions
            }

            // Load current task
            repository.getCurrentTask().collect { task ->
                _currentTask.value = task
            }
        }
    }
    // Weekly data
    val weeklySessions = repository.getSessionsBetweenDates(
        LocalDate.now().minusDays(7),
        LocalDate.now()
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Timer control functions
    fun startTimer() {
        timerState = TimerState.RUNNING
        showTimerNotification()
        startTimerCountdown()
    }

    fun pauseTimer() {
        timerState = TimerState.PAUSED
        cancelNotification()
    }

    fun resetTimer() {
        timerState = TimerState.IDLE
        timeRemaining = when (currentSessionType) {
            SessionType.POMODORO -> pomodoroDuration * 60
            SessionType.SHORT_BREAK -> shortBreakDuration * 60
            SessionType.LONG_BREAK -> longBreakDuration * 60
        }
    }
    private fun startTimerCountdown() {
        viewModelScope.launch {
            while (timerState == TimerState.RUNNING) {
                delay(1000)
                if (timeRemaining > 0) {
                    timeRemaining--
                } else {
                    endTimer()
                }
            }
        }
    }
    private fun endTimer() {
        timerState = TimerState.FINISHED
        viewModelScope.launch {
            // Save session
            val session = FocusSession(
                startTime = LocalDateTime.now().minusSeconds(timeRemaining.toLong()),
                endTime = LocalDateTime.now(),
                durationMinutes = timeRemaining / 60,
                sessionType = currentSessionType,
                completed = true
            )
            repository.insertSession(session)

            // Update today's sessions
            _todaySessions.value += session

            // Check for new badges
            checkForNewBadges()

            // Show completion notification
            showCompletionNotification()
        }
    }

    private fun cancelNotification() {
        notificationService.cancelNotifications()
    }

    private fun showCompletionNotification() {
        notificationService.showTimerCompleteNotification(currentSessionType)
    }


    fun skipToNextSession() {
        viewModelScope.launch {
            if (currentSessionType == SessionType.POMODORO && timerState != TimerState.IDLE) {
                // Save completed pomodoro
                val now = LocalDateTime.now()
                val session = FocusSession(
                    startTime = now.minusSeconds((pomodoroDuration * 60 - timeRemaining).toLong()),
                    endTime = now,
                    durationMinutes = pomodoroDuration,
                    sessionType = SessionType.POMODORO,
                    completed = true
                )
                repository.insertSession(session)

                // Update stats
                val stats = userStats.value ?: UserStats()
                val newStats = stats.copy(
                    totalPomodoros = stats.totalPomodoros + 1,
                    totalFocusMinutes = stats.totalFocusMinutes + pomodoroDuration,
                    experience = stats.experience + 10 // 10 XP per pomodoro
                )
                repository.updateStats(newStats)

                completedPomodorosInSet++
            }

            // Determine next session type
            currentSessionType = when {
                currentSessionType != SessionType.POMODORO -> SessionType.POMODORO
                completedPomodorosInSet >= pomodorosUntilLongBreak -> {
                    completedPomodorosInSet = 0
                    SessionType.LONG_BREAK
                }
                else -> SessionType.SHORT_BREAK
            }

            // Reset timer for new session
            resetTimer()
        }
    }

    fun updateTimerSettings(
        pomodoro: Int,
        shortBreak: Int,
        longBreak: Int,
        pomodorosUntilLong: Int
    ) {
        pomodoroDuration = pomodoro
        shortBreakDuration = shortBreak
        longBreakDuration = longBreak
        pomodorosUntilLongBreak = pomodorosUntilLong
        resetTimer() // Reset with new durations
    }

    fun decrementTime() {
        if (timerState == TimerState.RUNNING && timeRemaining > 0) {
            timeRemaining--
            if (timeRemaining <= 0) {
                timerState = TimerState.FINISHED
                skipToNextSession()
            }
        }
    }

// Task management
    val currentTask = repository.getCurrentTask()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val allTasks = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Badges
    val unlockedBadges = userStats.map { stats ->
        allBadges.filter { badge -> stats?.unlockedBadges?.contains(badge.id) ?: false }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    private fun showTimerNotification() {
        val minutes = TimeUnit.SECONDS.toMinutes(timeRemaining.toLong())
        val seconds = timeRemaining % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)

        notificationService.showTimerNotification(currentSessionType, timeString)
    }

    // Task functions
    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(completed = true)
            repository.updateTask(updatedTask)
        }
    }

    // Firebase sync
    fun syncData() {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId()
            repository.syncWithFirebase(userId)
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId()
            repository.fetchFromFirebase(userId)
        }
    }

    // Check for new badges
//    private fun checkForNewBadges(stats: UserStats) {
//        viewModelScope.launch {
//            val newBadges = repository.checkForNewBadges(stats)
//            repository.get
//            if (newBadges.isNotEmpty()) {
//                val updatedBadges = stats.unlockedBadges + newBadges.map { it.id }
//                val updatedStats = stats.copy(unlockedBadges = updatedBadges)
//                repository.updateStats(updatedStats)
//
//                // Show badge unlock animation
//                _newBadgesUnlocked.value = newBadges
//
//            }
//        }
//    }

    private fun checkForNewBadges() {
        viewModelScope.launch {
            repository.getUserStats(repository.getCurrentUserId()).collect { stats ->
                stats?.let {
                    val newBadges = repository.checkForNewBadges(it)
                    if (newBadges.isNotEmpty()) {
                        _newBadgesUnlocked.value = newBadges
                    }
                }
            }
        }
    }

    fun clearNewBadges() {
        _newBadgesUnlocked.value = emptyList()
    }
}

enum class TimerState {
    IDLE, RUNNING, PAUSED, FINISHED
}