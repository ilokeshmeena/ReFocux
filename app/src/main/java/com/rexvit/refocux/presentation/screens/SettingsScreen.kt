package com.rexvit.refocux.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rexvit.refocux.presentation.viewmodel.FocusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FocusViewModel,
    onBackClick: () -> Unit
) {
    var pomodoroDuration by remember { mutableStateOf(viewModel.pomodoroDuration) }
    var shortBreakDuration by remember { mutableStateOf(viewModel.shortBreakDuration) }
    var longBreakDuration by remember { mutableStateOf(viewModel.longBreakDuration) }
    var pomodorosUntilLongBreak by remember { mutableStateOf(viewModel.pomodorosUntilLongBreak) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Timer Settings", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            DurationSetting(
                label = "Pomodoro Duration (minutes)",
                value = pomodoroDuration,
                onValueChange = { pomodoroDuration = it },
                range = 1..60
            )

            DurationSetting(
                label = "Short Break Duration (minutes)",
                value = shortBreakDuration,
                onValueChange = { shortBreakDuration = it },
                range = 1..30
            )

            DurationSetting(
                label = "Long Break Duration (minutes)",
                value = longBreakDuration,
                onValueChange = { longBreakDuration = it },
                range = 5..60
            )

            NumberSetting(
                label = "Pomodoros until long break",
                value = pomodorosUntilLongBreak,
                onValueChange = { pomodorosUntilLongBreak = it },
                range = 1..10
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.updateTimerSettings(
                        pomodoroDuration,
                        shortBreakDuration,
                        longBreakDuration,
                        pomodorosUntilLongBreak
                    )
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
    }
}

@Composable
private fun DurationSetting(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1,
            modifier = Modifier.fillMaxWidth()
        )
        Text("$value minutes", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun NumberSetting(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { if (value > range.first) onValueChange(value - 1) },
                enabled = value > range.first
            ) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }

            Text("$value", style = MaterialTheme.typography.titleMedium)

            IconButton(
                onClick = { if (value < range.last) onValueChange(value + 1) },
                enabled = value < range.last
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}