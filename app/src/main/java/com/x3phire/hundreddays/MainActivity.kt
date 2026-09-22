package com.x3phire.hundreddays

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.core.util.Consumer
import androidx.navigation.compose.rememberNavController
import com.x3phire.hundreddays.ui.HundredDaysNav
import com.x3phire.hundreddays.ui.theme.AppBackground
import com.x3phire.hundreddays.ui.theme.HundredDaysTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val service = (application as HundredDaysApp).container.journalService
        setContent {
            HundredDaysTheme {
                AppBackground {
                    val navController = rememberNavController()
                    DisposableEffect(navController) {
                        val listener = Consumer<Intent> { incoming ->
                            navController.handleDeepLink(incoming)
                        }
                        addOnNewIntentListener(listener)
                        navController.handleDeepLink(intent)
                        onDispose { removeOnNewIntentListener(listener) }
                    }
                    HundredDaysNav(
                        service = service,
                        navController = navController,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
