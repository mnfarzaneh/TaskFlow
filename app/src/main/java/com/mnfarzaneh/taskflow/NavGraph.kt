package com.mnfarzaneh.taskflow

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.compose.*
import com.mnfarzaneh.taskflow.ui.calendar.CalendarScreen
import com.mnfarzaneh.taskflow.ui.chain.ChainScreen
import com.mnfarzaneh.taskflow.ui.chain.CreateChainScreen
import com.mnfarzaneh.taskflow.ui.chain.EditChainScreen
import com.mnfarzaneh.taskflow.ui.home.HomeScreen
import com.mnfarzaneh.taskflow.ui.task.TaskDetailScreen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mnfarzaneh.taskflow.ui.theme.GlassBackground
import com.mnfarzaneh.taskflow.ui.theme.Matcha800
import com.mnfarzaneh.taskflow.ui.theme.Matcha700
import com.mnfarzaneh.taskflow.ui.theme.Matcha100
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Chain : Screen("chain/{chainId}") {
        fun createRoute(chainId: Long) = "chain/$chainId"
    }

    object Task : Screen("task/{taskId}") {
        fun createRoute(taskId: Long) = "task/$taskId"
    }

    object CreateChain : Screen("create_chain")
    object EditChain : Screen("edit_chain/{chainId}") {
        fun createRoute(chainId: Long) = "edit_chain/$chainId"
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun TaskFlowNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem("خانه", Icons.Default.Home, Screen.Home.route),
        BottomNavItem("تقویم", Icons.Default.DateRange, Screen.Calendar.route)
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Calendar.route
    )
    GlassBackground {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = Color(0xCCEFF6E2),  // ← سبز ماچا با شفافیت
                        contentColor   = Matcha800,
                        tonalElevation = 0.dp,
                        modifier       = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)  // ← فاصله از لبه‌ها
                            .clip(RoundedCornerShape(24.dp))                // ← گوشه‌های گرد
                            .border(
                                width = 1.5.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x88FFFFFF),
                                        Color(0x33FFFFFF)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ){
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(22.dp)   // ← آیکون کوچیک‌تر
                                    )
                                },
                                label = {
                                    Text(
                                        item.label,
                                        style = MaterialTheme.typography.labelSmall  // ← متن کوچیک‌تر
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Matcha700,
                                    selectedTextColor = Matcha700,
                                    indicatorColor = Matcha100,
                                    unselectedIconColor = Color(0xFF8B8F84),
                                    unselectedTextColor = Color(0xFF8B8F84)
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(padding),
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
                }
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onChainClick = { chainId ->
                            navController.navigate(Screen.Chain.createRoute(chainId))
                        },
                        onAddChain = {
                            navController.navigate(Screen.CreateChain.route)
                        }
                    )
                }

                composable(Screen.Calendar.route) {
                    CalendarScreen(
                        onTaskClick = { taskId ->
                            navController.navigate(Screen.Task.createRoute(taskId))
                        }
                    )
                }

                composable(
                    route = Screen.Chain.route,
                    arguments = listOf(navArgument("chainId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val chainId = backStackEntry.arguments?.getLong("chainId") ?: return@composable
                    ChainScreen(
                        onBack = { navController.popBackStack() },
                        onTaskClick = { taskId ->
                            navController.navigate(Screen.Task.createRoute(taskId))
                        },
                        onEdit = {
                            navController.navigate(Screen.EditChain.createRoute(chainId))
                        }
                    )
                }

                composable(
                    route = Screen.Task.route,
                    arguments = listOf(navArgument("taskId") { type = NavType.LongType })
                ) {
                    TaskDetailScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.CreateChain.route) {
                    CreateChainScreen(
                        onBack = { navController.popBackStack() },
                        onChainCreated = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.EditChain.route,
                    arguments = listOf(navArgument("chainId") { type = NavType.LongType })
                ) {
                    EditChainScreen(
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}