package com.calendar.ddcalendar.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.calendar.ddcalendar.data.model.CalendarDate
import com.calendar.ddcalendar.ui.calendar.month.MonthScreen
import com.calendar.ddcalendar.ui.calendar.week.WeekScreen
import com.calendar.ddcalendar.ui.calendar.day.DayScreen

/**
 * 导航图
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Month.route,
    showBottomBar: Boolean = true
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                Icons.Default.CalendarMonth, 
                                contentDescription = "月视图"
                            ) 
                        },
                        label = { 
                            Text(
                                "月",
                                style = MaterialTheme.typography.labelMedium
                            ) 
                        },
                        selected = currentRoute == Screen.Month.route,
                        onClick = {
                            if (currentRoute != Screen.Month.route) {
                                navController.navigate(Screen.Month.route) {
                                    // 清除返回栈，避免重复导航
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                Icons.Default.CalendarViewWeek, 
                                contentDescription = "周视图"
                            ) 
                        },
                        label = { 
                            Text(
                                "周",
                                style = MaterialTheme.typography.labelMedium
                            ) 
                        },
                        selected = currentRoute == Screen.Week.route,
                        onClick = {
                            if (currentRoute != Screen.Week.route) {
                                navController.navigate(Screen.Week.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                Icons.Default.CalendarToday, 
                                contentDescription = "日视图"
                            ) 
                        },
                        label = { 
                            Text(
                                "日",
                                style = MaterialTheme.typography.labelMedium
                            ) 
                        },
                        selected = currentRoute == Screen.Day.route,
                        onClick = {
                            if (currentRoute != Screen.Day.route) {
                                navController.navigate(Screen.Day.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // 月视图
            composable(Screen.Month.route) {
                MonthScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    },
                    onAddEventClick = { date ->
                        navController.navigate(Screen.EventEditor.createRouteWithDate(date))
                    }
                )
            }

            // 周视图
            composable(Screen.Week.route) {
                WeekScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    },
                    onAddEventClick = { date ->
                        navController.navigate(Screen.EventEditor.createRouteWithDate(date))
                    }
                )
            }

            // 日视图
            composable(Screen.Day.route) {
                DayScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    },
                    onAddEventClick = { date ->
                        navController.navigate(Screen.EventEditor.createRouteWithDate(date))
                    }
                )
            }

            // 事件详情
            composable(
                route = Screen.EventDetail.route,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: return@composable
                com.calendar.ddcalendar.ui.event.EventDetailScreen(
                    eventId = eventId,
                    onNavigateBack = { navController.popBackStack() },
                    onEditClick = { id ->
                        navController.navigate(Screen.EventEditor.createRoute(id))
                    }
                )
            }

            // 事件编辑
            composable(
                route = Screen.EventEditor.route,
                arguments = listOf(
                    navArgument("eventId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("year") {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument("month") {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument("day") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId")?.takeIf { it != -1L }
                val year = backStackEntry.arguments?.getInt("year")?.takeIf { it != -1 }
                val month = backStackEntry.arguments?.getInt("month")?.takeIf { it != -1 }
                val day = backStackEntry.arguments?.getInt("day")?.takeIf { it != -1 }
                
                val initialDate = if (year != null && month != null && day != null) {
                    CalendarDate(year, month, day)
                } else {
                    null
                }
                
                com.calendar.ddcalendar.ui.event.EventEditorScreen(
                    eventId = eventId,
                    initialDate = initialDate,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * 导航屏幕定义
 */
sealed class Screen(val route: String) {
    object Month : Screen("month")
    object Week : Screen("week")
    object Day : Screen("day")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: Long) = "event_detail/$eventId"
    }
    object EventEditor : Screen("event_editor?eventId={eventId}&year={year}&month={month}&day={day}") {
        fun createRoute(eventId: Long? = null) = if (eventId != null) {
            "event_editor?eventId=$eventId"
        } else {
            "event_editor"
        }
        
        fun createRouteWithDate(date: CalendarDate, eventId: Long? = null): String {
            val eventIdParam = if (eventId != null) "eventId=$eventId&" else ""
            return "event_editor?${eventIdParam}year=${date.year}&month=${date.month}&day=${date.day}"
        }
    }
}
