package com.rexvit.refocux.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rexvit.refocux.presentation.screens.BadgesScreen
import com.rexvit.refocux.presentation.screens.SettingsScreen
import com.rexvit.refocux.presentation.screens.StatsScreen
import com.rexvit.refocux.presentation.screens.TaskScreen
import com.rexvit.refocux.presentation.screens.TimerScreen
import com.rexvit.refocux.presentation.viewmodel.FocusViewModel

@Composable
fun ReFocuxNavGraph(
    viewModel: FocusViewModel,
    navController: NavHostController = rememberNavController()
) {
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(
            viewModel = viewModel,
            onBackClick = { showSettings = false }
        )
    } else {
        NavHost(
            navController = navController,
            startDestination = "timer"
        ) {
            composable("timer") {
                TimerScreen(
                    viewModel = viewModel,
                    onSettingsClick = { showSettings = true },
                    onTasksClick = { navController.navigate("tasks") },
                    onStatsClick = { navController.navigate("stats") },
                    onBadgesClick = { navController.navigate("badges") }
                )
            }
            composable("stats") {
                StatsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("tasks") {
                TaskScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("badges") {
                BadgesScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}