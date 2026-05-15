package com.gymia.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymia.ui.ai.AiCycleScreen
import com.gymia.ui.cardio.CardioScreen
import com.gymia.ui.history.HistoryScreen
import com.gymia.ui.progress.ProgressScreen
import com.gymia.ui.workout.ActiveSessionScreen
import com.gymia.ui.workout.CreatePlanScreen
import com.gymia.ui.workout.ExerciseListScreen
import com.gymia.ui.workout.WorkoutScreen

sealed class Screen(val route: String) {
    object Workout : Screen("workout")
    object History : Screen("history")
    object Progress : Screen("progress")
    object Cardio : Screen("cardio")
    object AiCycle : Screen("ai_cycle")
}

private data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Workout, "Workout", Icons.Default.FitnessCenter),
    BottomNavItem(Screen.History, "History", Icons.Default.History),
    BottomNavItem(Screen.Progress, "Progress", Icons.AutoMirrored.Filled.TrendingUp),
    BottomNavItem(Screen.Cardio, "Cardio", Icons.AutoMirrored.Filled.DirectionsRun),
    BottomNavItem(Screen.AiCycle, "AI", Icons.Default.AutoAwesome)
)

private val bottomNavRoutes = bottomNavItems.map { it.screen.route }.toSet()

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        AppNavHost(navController = navController, modifier = Modifier.padding(padding))
    }
}

@Composable
private fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Workout.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Screen.Workout.route) {
            WorkoutScreen(
                onStartSession = { dayId -> navController.navigate("active_session/$dayId") },
                onCreatePlan = { navController.navigate("create_plan") },
                onManageExercises = { navController.navigate("exercise_list") }
            )
        }
        composable(Screen.History.route) { HistoryScreen() }
        composable(Screen.Progress.route) { ProgressScreen() }
        composable(Screen.Cardio.route) { CardioScreen() }
        composable(Screen.AiCycle.route) { AiCycleScreen() }

        composable("exercise_list") {
            ExerciseListScreen(onBack = { navController.popBackStack() })
        }
        composable("create_plan") {
            CreatePlanScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = "active_session/{dayId}",
            arguments = listOf(navArgument("dayId") { type = NavType.LongType })
        ) {
            ActiveSessionScreen(onFinished = { navController.popBackStack() })
        }
    }
}

@Composable
private fun AppBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = { onNavigate(item.screen.route) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) }
            )
        }
    }
}
