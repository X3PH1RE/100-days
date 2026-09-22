package com.x3phire.hundreddays.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.x3phire.hundreddays.domain.ChallengeState
import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.JournalService
import com.x3phire.hundreddays.ui.day.DayScreen
import com.x3phire.hundreddays.ui.grid.GridScreen
import com.x3phire.hundreddays.ui.onboarding.OnboardingScreen
import com.x3phire.hundreddays.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DAY = "day/{day}"

    fun day(day: DayKey): String = "day/${day.value}"
}

@Composable
fun HundredDaysNav(
    service: JournalService,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    val challenge by service.observeChallenge().collectAsStateWithLifecycle(
        initialValue = ChallengeState.NotConfigured,
    )

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
    ) {
        composable(Routes.HOME) {
            when (val state = challenge) {
                ChallengeState.NotConfigured -> {
                    OnboardingScreen(service = service)
                }
                is ChallengeState.Active -> {
                    GridScreen(
                        overview = state.overview,
                        onDayClick = { day -> navController.navigate(Routes.day(day)) },
                        onSettings = { navController.navigate(Routes.SETTINGS) },
                    )
                }
            }
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                service = service,
                challenge = challenge,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.DAY,
            arguments = listOf(navArgument("day") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "hundreddays://day/{day}" },
            ),
        ) { entry ->
            val raw = entry.arguments?.getString("day").orEmpty()
            val day = DayKey.parse(raw)
            if (day == null) {
                androidx.compose.material3.Text("Invalid day link")
            } else {
                DayScreen(
                    day = day,
                    service = service,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
