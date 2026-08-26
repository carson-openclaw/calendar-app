package com.omnipaws.calendar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.omnipaws.calendar.ui.screens.CalendarScreen
import com.omnipaws.calendar.ui.screens.EventsListScreen
import java.time.LocalDate

object Routes {
    const val CALENDAR = "calendar"
    const val EVENTS = "events/{date}"

    fun events(date: String) = "events/$date"
}

@Composable
fun AppNav(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.CALENDAR,
        modifier = modifier
    ) {
        composable(Routes.CALENDAR) {
            CalendarScreen(
                onDayClick = { date ->
                    navController.navigate(Routes.events(date.toString()))
                }
            )
        }

        composable(
            route = Routes.EVENTS,
            arguments = listOf(
                navArgument("date") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
                ?: LocalDate.now().toString()

            EventsListScreen(
                date = date,
                onBack = { navController.popBackStack() },
                onEventClick = { /* future: event detail */ }
            )
        }
    }
}
