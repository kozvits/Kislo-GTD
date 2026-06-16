package com.kozvits.kislogtd.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kozvits.kislogtd.presentation.control.ControlScreen
import com.kozvits.kislogtd.presentation.dashboard.DashboardScreen
import com.kozvits.kislogtd.presentation.day.DayScreen
import com.kozvits.kislogtd.presentation.inbox.InboxScreen
import com.kozvits.kislogtd.presentation.later.LaterScreen
import com.kozvits.kislogtd.presentation.maybe.MaybeScreen
import com.kozvits.kislogtd.presentation.project.ProjectScreen
import com.kozvits.kislogtd.presentation.project.ProjectDetailScreen
import com.kozvits.kislogtd.presentation.review.DailyReviewScreen
import com.kozvits.kislogtd.presentation.review.WeeklyReviewScreen
import com.kozvits.kislogtd.presentation.completed.CompletedTasksScreen
import com.kozvits.kislogtd.presentation.deleted.DeletedTasksScreen
import com.kozvits.kislogtd.presentation.taskdetail.TaskDetailScreen
import com.kozvits.kislogtd.presentation.settings.SettingsScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Успеватель", Icons.Filled.Home)
    data object Inbox : Screen("inbox", "***IN", Icons.Filled.Inbox)
    data object Day : Screen("day", "**DAY", Icons.Filled.Today)
    data object Control : Screen("control", "*CONTROL", Icons.Filled.Visibility)
    data object Later : Screen("later", "**LATER", Icons.Filled.Schedule)
    data object Maybe : Screen("maybe", ">>MAYBE", Icons.Filled.AutoAwesome)
    data object ProjectList : Screen("projects", "Проекты", Icons.Filled.Workspaces)
    data object ProjectDetail : Screen("project/{projectId}", "Проект", Icons.Filled.Workspaces)
    data object TaskDetail : Screen("task/{taskId}", "Задача", Icons.Filled.Task)
    data object DailyReview : Screen("daily_review", "Утренний регламент", Icons.Filled.Today)
    data object WeeklyReview : Screen("weekly_review", "Недельный обзор", Icons.Filled.Assessment)
    data object CompletedTasks : Screen("completed_tasks", "Выполненные задачи", Icons.Filled.CheckCircle)
    data object DeletedTasks : Screen("deleted_tasks", "Удаленные задачи", Icons.Filled.Delete)
    data object Settings : Screen("settings", "Настройки", Icons.Filled.Settings)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Inbox,
    Screen.Day,
    Screen.Control
)

val drawerNavItems = listOf(
    Screen.Later,
    Screen.Maybe,
    Screen.ProjectList,
    Screen.DailyReview,
    Screen.WeeklyReview,
    Screen.CompletedTasks,
    Screen.DeletedTasks,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Determine if we're on a bottom nav screen
    val isBottomNavScreen = bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    val currentScreen = bottomNavItems.find { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    } ?: drawerNavItems.find { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Успеватель",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))

                drawerNavItems.forEach { screen ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(text = screen.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (isBottomNavScreen) {
                    NavigationBar {
                        bottomNavItems.forEach { screen ->
                            val selected =
                                currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(text = screen.title, maxLines = 1) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        TextButton(onClick = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Главная",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = currentScreen?.title ?: "Успеватель",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Меню"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(padding),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(navController)
                }
                composable(Screen.Inbox.route) {
                    InboxScreen(navController)
                }
                composable(Screen.Day.route) {
                    DayScreen(navController)
                }
                composable(Screen.Control.route) {
                    ControlScreen(navController)
                }
                composable(Screen.Later.route) {
                    LaterScreen(navController)
                }
                composable(Screen.Maybe.route) {
                    MaybeScreen(navController)
                }
                composable(Screen.ProjectList.route) {
                    ProjectScreen(navController)
                }
                composable(
                    route = Screen.ProjectDetail.route,
                    arguments = listOf(
                        navArgument("projectId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    ProjectDetailScreen(projectId = projectId, navController = navController)
                }
                composable(
                    route = Screen.TaskDetail.route,
                    arguments = listOf(
                        navArgument("taskId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                    TaskDetailScreen(taskId = taskId, navController = navController)
                }
                composable(Screen.DailyReview.route) {
                    DailyReviewScreen(navController)
                }
                composable(Screen.WeeklyReview.route) {
                    WeeklyReviewScreen(navController)
                }
                composable(Screen.CompletedTasks.route) {
                    CompletedTasksScreen(navController)
                }
                composable(Screen.DeletedTasks.route) {
                    DeletedTasksScreen(navController = navController)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(navController)
                }
            }
        }
    }
}
