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
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
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
import androidx.glance.layout.size
import androidx.glance.layout.width
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
import com.x3phire.hundreddays.domain.WeekSlot
import kotlinx.coroutines.flow.first

private val DayKeyParam = ActionParameters.Key<String>("day_key")

class ContributionGridWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = runCatching {
            val app = context.applicationContext as? HundredDaysApp
                ?: error("Application is not HundredDaysApp")
            app.container.journalService.observeChallenge().first()
        }.getOrElse { ChallengeState.NotConfigured }

        provideContent {
            when (state) {
                ChallengeState.NotConfigured -> SetupPrompt()
                is ChallengeState.Active -> ActiveGrid(state.overview)
            }
        }
    }
}

class ContributionGridWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ContributionGridWidget()
}

class OpenDayAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val raw = parameters[DayKeyParam] ?: return
        val day = DayKey.parse(raw) ?: return
        context.startActivity(dayIntent(context, day))
    }
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
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun ActiveGrid(overview: ContributionGridModel) {
    val weeks = overview.asWeekColumns().takeLast(MaxWidgetWeeks)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(PaperProvider)
            .padding(10.dp),
    ) {
        DaysLeftHeader(
            daysLeft = overview.daysLeft,
            corner = overview.corner,
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            weeks.forEach { week ->
                Column {
                    week.slots.forEach { slot ->
                        when (slot) {
                            WeekSlot.Padding -> {
                                Spacer(
                                    modifier = GlanceModifier
                                        .size(CellSize)
                                        .padding(1.dp),
                                )
                            }
                            is WeekSlot.Day -> {
                                CellBlock(
                                    cell = slot.cell,
                                    onOpen = actionRunCallback<OpenDayAction>(
                                        actionParametersOf(DayKeyParam to slot.cell.day.toString()),
                                    ),
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = GlanceModifier.width(2.dp))
            }
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
    onOpen: Action,
) {
    val isToday = cell.phase == DayPhase.TODAY
    val alpha = if (cell.phase == DayPhase.FUTURE) 0.4f else 1f
    val fill = cell.intensity.toGlanceColor(alpha)
    Box(
        modifier = GlanceModifier
            .size(CellSize)
            .padding(1.dp)
            .cornerRadius(2.dp)
            .background(if (isToday) TodayRingProvider else fill)
            .padding(if (isToday) 1.dp else 0.dp)
            .cornerRadius(1.dp)
            .background(fill)
            .clickable(onOpen),
    ) {
    }
}

private fun Intensity.toGlanceColor(alpha: Float): GlanceColor {
    val base = when (this) {
        Intensity.EMPTY -> Color(0xFFEEF3F8)
        Intensity.LOW -> Color(0xFFD7E8FA)
        Intensity.MEDIUM -> Color(0xFF7EB6F6)
        Intensity.HIGH -> Color(0xFF2F6FED)
        Intensity.MAX -> Color(0xFF1A4B8C)
    }
    val tinted = base.copy(alpha = alpha)
    return ColorProvider(day = tinted, night = tinted)
}

private const val MaxWidgetWeeks = 16
private val CellSize = 11.dp

private val PaperProvider: GlanceColor = ColorProvider(day = Color(0xFFF4F7FB), night = Color(0xFFF4F7FB))
private val InkProvider: GlanceColor = ColorProvider(day = Color(0xFF0B1F33), night = Color(0xFF0B1F33))
private val TodayRingProvider: GlanceColor = ColorProvider(day = Color(0xFF0B1F33), night = Color(0xFF0B1F33))

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
