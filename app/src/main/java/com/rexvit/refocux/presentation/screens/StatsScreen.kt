package com.rexvit.refocux.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.rexvit.refocux.data.model.FocusSession
import com.rexvit.refocux.presentation.viewmodel.FocusViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: FocusViewModel,
    onBackClick: () -> Unit
) {
    val userStats by viewModel.userStats.collectAsState()
    val weeklySessions by viewModel.weeklySessions.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Your Stats") },
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
            // Summary cards
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Total Pomodoros",
                    value = userStats?.totalPomodoros?.toString() ?: "0",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatCard(
                    title = "Current Streak",
                    value = userStats?.currentStreak?.toString() ?: "0",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Total Focus Time",
                    value = formatMinutes(userStats?.totalFocusMinutes ?: 0),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatCard(
                    title = "Level",
                    value = userStats?.level?.toString() ?: "1",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Weekly chart
            Text("Weekly Focus Time", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            val chartData = remember(weeklySessions) {
                createWeeklyChartData(weeklySessions)
            }

            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBarShadow(false)
                        setDrawValueAboveBar(true)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            valueFormatter = IndexAxisValueFormatter(getWeekDays())
                        }

                        axisLeft.apply {
                            setDrawGridLines(true)
                            axisMinimum = 0f
                        }

                        axisRight.isEnabled = false
                        legend.isEnabled = false

                        data = chartData
                        animateY(1000)
                        invalidate()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

private fun createWeeklyChartData(sessions: List<FocusSession>): BarData {
    val days = getWeekDays()
    val entries = mutableListOf<BarEntry>()

    // Group sessions by day
    val dailyMinutes = mutableMapOf<Int, Float>().withDefault { 0f }

    sessions.groupBy { it.startTime.toLocalDate() }.forEach { (date, daySessions) ->
        val dayOfWeek = date.dayOfWeek.value % 7 // Convert to 0-6 (Sun-Sat)
        val totalMinutes = daySessions.sumOf { it.durationMinutes }.toFloat()
        dailyMinutes[dayOfWeek] = dailyMinutes.getValue(dayOfWeek) + totalMinutes
    }

    // Create entries
    days.indices.forEach { index ->
        entries.add(BarEntry(index.toFloat(), dailyMinutes.getValue(index)))
    }

    val dataSet = BarDataSet(entries, "Focus Minutes").apply {
        color = Color(0xFF6200EE).hashCode()
        valueTextColor = Color.Black.hashCode()
        valueTextSize = 10f
    }

    return BarData(dataSet)
}

private fun getWeekDays(): List<String> {
    val formatter = DateTimeFormatter.ofPattern("EEE")
    val today = LocalDate.now()
    return (0..6).map { today.minusDays((6 - it).toLong()).format(formatter) }
}

private fun formatMinutes(minutes: Long): String {
    return if (minutes < 60) {
        "${minutes}m"
    } else {
        "${minutes / 60}h ${minutes % 60}m"
    }
}