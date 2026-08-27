package com.omnipaws.calendar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.omnipaws.calendar.ui.screens.AssistantScreen
import com.omnipaws.calendar.ui.screens.CalendarScreen
import com.omnipaws.calendar.ui.screens.EventDetailScreen
import com.omnipaws.calendar.ui.screens.EventsListScreen
import com.omnipaws.calendar.ui.screens.SyncScreen
import java.time.LocalDate

object Routes {
    const val CALENDAR = "calendar"
    const val EVENTS = "events/{date}"
    const val EVENT = "event/{eventId}"
    const val ASSISTANT = "assistant"
    const val SYNC = "sync"

    fun events(date: String) = "events/$date"
    fun event(eventId: String) = "event/$eventId"
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
                },
                onEventClick = { event ->
                    navController.navigate(Routes.event(event.id))
                },
                onAssistantClick = {
                    navController.navigate(Routes.ASSISTANT)
                },
                onSyncClick = {
                    navController.navigate(Routes.SYNC)
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
                onEventClick = { eventId ->
                    navController.navigate(Routes.event(eventId))
                }
            )
        }

        composable(
            route = Routes.EVENT,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""

            EventDetailScreen(
                eventId = eventId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ASSISTANT) {
            AssistantScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SYNC) {
            SyncScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
