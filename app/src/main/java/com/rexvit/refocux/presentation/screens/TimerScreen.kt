package com.rexvit.refocux.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rexvit.refocux.data.model.Badge
import com.rexvit.refocux.data.model.SessionType
import com.rexvit.refocux.data.model.SessionType.*
import com.rexvit.refocux.presentation.viewmodel.FocusViewModel
import com.rexvit.refocux.presentation.viewmodel.TimerState
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TimerScreen(
    viewModel: FocusViewModel,
    onSettingsClick: () -> Unit,
    onTasksClick: () -> Unit,
    onStatsClick: () -> Unit,
    onBadgesClick: () -> Unit
) {

    // Existing state collection...
    val timerState by rememberUpdatedState(viewModel.timerState)
    val timeRemaining by rememberUpdatedState(viewModel.timeRemaining)
    val sessionType by rememberUpdatedState(viewModel.currentSessionType)
    val userStats by viewModel.userStats.collectAsState()

    // New badge animation
    val newBadges by viewModel.newBadgesUnlocked.collectAsState()
    var showBadgeAnimation by remember { mutableStateOf(false) }
    var currentBadge by remember { mutableStateOf<Badge?>(null) }

//    LaunchedEffect(viewModel.timerState) {
//        while (viewModel.timerState == TimerState.RUNNING) {
//            delay(1000) // 1 second delay
//            viewModel.decrementTime()
//        }
//    }

    LaunchedEffect(newBadges) {
        if (newBadges.isNotEmpty()) {
            showBadgeAnimation = true
            currentBadge = newBadges.first()
            delay(3000) // Show animation for 3 seconds
            showBadgeAnimation = false
            viewModel.clearNewBadges() // Implement this in ViewModel
        }
    }

    // Background animation based on timer state
    val infiniteTransition = rememberInfiniteTransition()
    val backgroundAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = when (sessionType) {
                        POMODORO -> MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha)
                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = backgroundAlpha)
                    }
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar with navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onTasksClick) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Tasks"
                    )
                }

                Text(
                    text = when (sessionType) {
                        POMODORO -> "Focus Session"
                        SHORT_BREAK -> "Short Break"
                        LONG_BREAK -> "Long Break"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onStatsClick) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Stats"
                    )
                }
            }

            // Timer display with enhanced animation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(300.dp)
                    .shadow(24.dp, MaterialTheme.shapes.extraLarge)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.extraLarge
                    )
            ) {
                val minutes = TimeUnit.SECONDS.toMinutes(timeRemaining.toLong())
                val seconds = timeRemaining % 60


                AnimatedContent(
                    targetState = timeRemaining,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()) with
                                (slideOutVertically { height -> -height } + fadeOut())
                    }
                ) { remaining ->
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.scale(pulse),
                        color = when (sessionType) {
                            POMODORO -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )
                }
            }

        // Timer controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = { onSettingsClick() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                when (timerState) {
                    TimerState.RUNNING -> {
                        IconButton(
                            onClick = { viewModel.pauseTimer() },
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    else -> {
                        IconButton(
                            onClick = { viewModel.startTimer() },
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.skipToNextSession() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Progress indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(viewModel.pomodorosUntilLongBreak) { index ->
                    val isCompleted = index < viewModel.completedPomodorosInSet
                    val isCurrent = index == viewModel.completedPomodorosInSet &&
                            sessionType == SessionType.POMODORO

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = when {
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    isCompleted -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }


            // Bottom navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = onBadgesClick) {
                    Icon(Icons.Default.Star, contentDescription = "Badges")
                }
            }
        }

        // Badge unlock animation
        if (showBadgeAnimation && currentBadge != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f))
                    .clickable { showBadgeAnimation = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Lottie animation for badge unlock
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎉",
                            style = MaterialTheme.typography.displayLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Badge Unlocked!",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = currentBadge?.name ?: "",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = currentBadge?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}