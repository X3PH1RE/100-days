package com.x3phire.hundreddays.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.x3phire.hundreddays.domain.Intensity

val ForestInk = Color(0xFF1B4332)
val ForestDeep = Color(0xFF2D6A4F)
val ForestMid = Color(0xFF40916C)
val ForestSoft = Color(0xFF74C69D)
val ForestMist = Color(0xFFD8F3DC)
val Paper = Color(0xFFF7FBF8)
val InkMuted = Color(0xFF52796F)

private val LightColors = lightColorScheme(
    primary = ForestDeep,
    onPrimary = Color.White,
    secondary = ForestMid,
    onSecondary = Color.White,
    background = Paper,
    onBackground = ForestInk,
    surface = Paper,
    onSurface = ForestInk,
    surfaceVariant = ForestMist,
    onSurfaceVariant = InkMuted,
)

fun Intensity.toCellColor(): Color =
    when (this) {
        Intensity.EMPTY -> Color(0xFFE9F5EE)
        Intensity.LOW -> ForestMist
        Intensity.MEDIUM -> ForestSoft
        Intensity.HIGH -> ForestMid
        Intensity.MAX -> ForestDeep
    }

@Composable
fun HundredDaysTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
