package com.x3phire.hundreddays.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider as GlanceColor
import com.x3phire.hundreddays.HundredDaysApp
import com.x3phire.hundreddays.MainActivity
import com.x3phire.hundreddays.domain.ChallengeState
import com.x3phire.hundreddays.domain.ContributionGridModel
import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.DayPhase
import com.x3phire.hundreddays.domain.DaysLeftCorner
import com.x3phire.hundreddays.domain.GridCell
import com.x3phire.hundreddays.domain.Intensity
import kotlinx.coroutines.flow.first

class ContributionGridWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val service = (context.applicationContext as HundredDaysApp).container.journalService
        val state = service.observeChallenge().first()
        provideContent {
            GlanceTheme {
                when (state) {
                    ChallengeState.NotConfigured -> SetupPrompt()
                    is ChallengeState.Active -> ActiveGrid(state.overview)
                }
            }
        }
    }
}

class ContributionGridWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ContributionGridWidget()
}

@Composable
private fun SetupPrompt() {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(PaperProvider)
            .padding(12.dp)
            .clickable(actionStartActivity(openAppIntent(context))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Open 100 Days to set your dates",
            style = TextStyle(
                color = InkProvider,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun ActiveGrid(overview: ContributionGridModel) {
    val context = LocalContext.current
    val columns = overview.settings.layout.columns
    val rows = overview.cells.chunked(columns)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(PaperProvider)
            .padding(8.dp),
    ) {
        DaysLeftHeader(
            daysLeft = overview.daysLeft,
            corner = overview.corner,
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        rows.forEach { rowCells ->
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                rowCells.forEach { cell ->
                    CellBlock(
                        cell = cell,
                        modifier = GlanceModifier.defaultWeight(),
                        onOpen = actionStartActivity(dayIntent(context, cell.day)),
                    )
                }
                repeat(columns - rowCells.size) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                }
            }
            Spacer(modifier = GlanceModifier.height(3.dp))
        }
    }
}

@Composable
private fun DaysLeftHeader(daysLeft: Int, corner: DaysLeftCorner) {
    val label = if (daysLeft == 0) "Complete" else "$daysLeft days left"
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        if (corner == DaysLeftCorner.TOP_END) {
            Spacer(modifier = GlanceModifier.defaultWeight())
        }
        Text(
            text = label,
            style = TextStyle(
                color = InkProvider,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        if (corner == DaysLeftCorner.TOP_START) {
            Spacer(modifier = GlanceModifier.defaultWeight())
        }
    }
}

@Composable
private fun CellBlock(
    cell: GridCell,
    modifier: GlanceModifier,
    onOpen: Action,
) {
    val alpha = if (cell.phase == DayPhase.FUTURE) 0.45f else 1f
    Box(
        modifier = modifier
            .height(14.dp)
            .padding(1.dp)
            .cornerRadius(2.dp)
            .background(cell.intensity.toGlanceColor(alpha))
            .clickable(onOpen),
    ) {
    }
}

private fun Intensity.toGlanceColor(alpha: Float): GlanceColor {
    val base = when (this) {
        Intensity.EMPTY -> Color(0xFFE9F5EE)
        Intensity.LOW -> Color(0xFFD8F3DC)
        Intensity.MEDIUM -> Color(0xFF74C69D)
        Intensity.HIGH -> Color(0xFF40916C)
        Intensity.MAX -> Color(0xFF2D6A4F)
    }
    val tinted = base.copy(alpha = alpha)
    return ColorProvider(day = tinted, night = tinted)
}

private val PaperProvider: GlanceColor = ColorProvider(day = Color(0xFFF7FBF8), night = Color(0xFFF7FBF8))
private val InkProvider: GlanceColor = ColorProvider(day = Color(0xFF1B4332), night = Color(0xFF1B4332))

internal fun dayIntent(context: Context, day: DayKey): Intent =
    Intent(Intent.ACTION_VIEW, dayUri(day), context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

internal fun openAppIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

internal fun dayUri(day: DayKey): Uri =
    Uri.parse("hundreddays://day/${day.value}")
