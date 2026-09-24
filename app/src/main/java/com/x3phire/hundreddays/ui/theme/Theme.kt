package com.x3phire.hundreddays.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.x3phire.hundreddays.domain.Intensity

val Ink = Color(0xFF0B1F33)
val InkMuted = Color(0xFF5B6F82)
val BlueDeep = Color(0xFF0F367D)
val BlueMid = Color(0xFF1E5FE6)
val BlueSoft = Color(0xFF428DF5)
val BlueMist = Color(0xFF90C2F9)
val BlueEmpty = Color(0xFFDCE6F0)
val CellBorder = Color(0xFFBACBDC)
val CellBorderFuture = Color(0xFFCBD8E6)
val CellFillFuture = Color(0xFFEAF1F8)
val Paper = Color(0xFFF4F7FB)
val PaperDeep = Color(0xFFE7EEF7)
val TodayRing = BlueDeep

private val LightColors = lightColorScheme(
    primary = BlueDeep,
    onPrimary = Color.White,
    secondary = BlueMid,
    onSecondary = Color.White,
    tertiary = BlueSoft,
    background = Paper,
    onBackground = Ink,
    surface = Color.White.copy(alpha = 0.72f),
    onSurface = Ink,
    surfaceVariant = BlueMist,
    onSurfaceVariant = InkMuted,
    outline = Color(0xFFB7C7D8),
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.3.sp,
    ),
)

fun Intensity.toCellColor(): Color =
    when (this) {
        Intensity.EMPTY -> BlueEmpty
        Intensity.LOW -> BlueMist
        Intensity.MEDIUM -> BlueSoft
        Intensity.HIGH -> BlueMid
        Intensity.MAX -> BlueDeep
    }

fun Intensity.toBorderColor(): Color =
    when (this) {
        Intensity.EMPTY -> CellBorder
        Intensity.LOW -> Color(0xFF6BAAF5)
        Intensity.MEDIUM -> Color(0xFF2571E0)
        Intensity.HIGH -> Color(0xFF1647B0)
        Intensity.MAX -> Color(0xFF0B2556)
    }

@Composable
fun HundredDaysTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content,
    )
}

@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Paper, PaperDeep, Color(0xFFDCE8F6)),
                    start = Offset(0f, 0f),
                    end = Offset(1200f, 1800f),
                ),
            ),
    ) {
        content()
    }
}
