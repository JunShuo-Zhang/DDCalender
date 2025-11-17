package com.calendar.ddcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.calendar.ddcalendar.ui.navigation.NavGraph
import com.calendar.ddcalendar.ui.navigation.Screen
import com.calendar.ddcalendar.ui.theme.DDCalendarTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主 Activity
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DDCalendarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    
                    // 只在主视图（月/周/日）显示底部导航栏
                    val showBottomBar = currentRoute?.let { route ->
                        route == Screen.Month.route || 
                        route == Screen.Week.route || 
                        route == Screen.Day.route
                    } ?: true // 默认显示（首次进入时）
                    
                    NavGraph(
                        navController = navController,
                        startDestination = Screen.Month.route, // 确保首次进入默认月视图
                        showBottomBar = showBottomBar
                    )
                }
            }
        }
    }
}