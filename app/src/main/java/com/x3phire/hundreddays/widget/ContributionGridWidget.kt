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
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
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
import com.x3phire.hundreddays.domain.WeekSlot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

private val DayKeyParam = ActionParameters.Key<String>("day_key")

class ContributionGridWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = runCatching {
            val app = context.applicationContext as? HundredDaysApp
                ?: error("Application is not HundredDaysApp")
            withTimeoutOrNull(2500L) {
                app.container.journalService.observeChallenge().first()
            } ?: ChallengeState.NotConfigured
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
            .cornerRadius(24.dp)
            .appWidgetBackground()
            .background(CardBackgroundProvider)
            .padding(16.dp)
            .clickable(actionStartActivity(openAppIntent(context))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "100 DAYS",
                style = TextStyle(
                    color = MutedInkProvider,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = GlanceModifier.height(6.dp))
            Box(
                modifier = GlanceModifier
                    .cornerRadius(12.dp)
                    .background(BadgeBackgroundProvider)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Open app to set your dates",
                    style = TextStyle(
                        color = InkProvider,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ActiveGrid(overview: ContributionGridModel) {
    val context = LocalContext.current
    val allWeeks = overview.asWeekColumns()
    // Fill all 16 weeks of the challenge so the entire 100-day window is displayed
    val maxWeeks = 16
    val weeks = if (allWeeks.size > maxWeeks) allWeeks.takeLast(maxWeeks) else allWeeks

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .appWidgetBackground()
            .background(CardBackgroundProvider)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        DaysLeftHeader(
            daysLeft = overview.daysLeft,
            corner = overview.corner,
            onOpenApp = actionStartActivity(openAppIntent(context)),
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Grid fills the remaining widget space proportionally
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            weeks.forEach { week ->
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    week.slots.forEach { slot ->
                        when (slot) {
                            WeekSlot.Padding -> {
                                Spacer(
                                    modifier = GlanceModifier
                                        .defaultWeight()
                                        .fillMaxWidth()
                                        .padding(1.5.dp),
                                )
                            }
                            is WeekSlot.Day -> {
                                CellBlock(
                                    cell = slot.cell,
                                    modifier = GlanceModifier
                                        .defaultWeight()
                                        .fillMaxWidth()
                                        .padding(1.5.dp),
                                    onOpen = actionRunCallback<OpenDayAction>(
                                        actionParametersOf(DayKeyParam to slot.cell.day.toString()),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DaysLeftHeader(
    daysLeft: Int,
    corner: DaysLeftCorner,
    onOpenApp: Action,
) {
    val label = when (daysLeft) {
        0 -> "Complete"
        1 -> "1 day left"
        else -> "$daysLeft days left"
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .clickable(onOpenApp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (corner == DaysLeftCorner.TOP_END) {
            Text(
                text = "100 DAYS",
                style = TextStyle(
                    color = MutedInkProvider,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
        }

        Box(
            modifier = GlanceModifier
                .cornerRadius(10.dp)
                .background(BadgeBackgroundProvider)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = TextStyle(
                    color = BadgeTextProvider,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        if (corner == DaysLeftCorner.TOP_START) {
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "100 DAYS",
                style = TextStyle(
                    color = MutedInkProvider,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

@Composable
private fun CellBlock(
    cell: GridCell,
    modifier: GlanceModifier,
    onOpen: Action,
) {
    val isToday = cell.phase == DayPhase.TODAY
    val isFuture = cell.phase == DayPhase.FUTURE
    val fill = cell.intensity.toGlanceFill(isFuture)
    val border = if (isToday) TodayRingProvider else cell.intensity.toGlanceBorder(isFuture)
    val borderWidth = if (isToday) 1.5.dp else 0.8.dp

    Box(
        modifier = modifier
            .cornerRadius(3.dp)
            .background(border)
            .padding(borderWidth)
            .clickable(onOpen),
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(2.dp)
                .background(fill),
        ) {}
    }
}

private fun Intensity.toGlanceFill(isFuture: Boolean): GlanceColor {
    return when (this) {
        Intensity.EMPTY -> {
            if (isFuture) {
                ColorProvider(day = Color(0xFFEDF3F8), night = Color(0xFF101722))
            } else {
                ColorProvider(day = Color(0xFFE2EAF2), night = Color(0xFF141D2B))
            }
        }
        Intensity.LOW -> {
            ColorProvider(day = Color(0xFF90C2F9), night = Color(0xFF1E40AF))
        }
        Intensity.MEDIUM -> {
            ColorProvider(day = Color(0xFF428DF5), night = Color(0xFF2563EB))
        }
        Intensity.HIGH -> {
            ColorProvider(day = Color(0xFF1E5FE6), night = Color(0xFF3B82F6))
        }
        Intensity.MAX -> {
            ColorProvider(day = Color(0xFF0F367D), night = Color(0xFF60A5FA))
        }
    }
}

private fun Intensity.toGlanceBorder(isFuture: Boolean): GlanceColor {
    return when (this) {
        Intensity.EMPTY -> {
            if (isFuture) {
                ColorProvider(day = Color(0xFFCBD8E6), night = Color(0xFF202E40))
            } else {
                ColorProvider(day = Color(0xFFBACBDC), night = Color(0xFF2C3E55))
            }
        }
        Intensity.LOW -> {
            ColorProvider(day = Color(0xFF6BAAF5), night = Color(0xFF3B82F6))
        }
        Intensity.MEDIUM -> {
            ColorProvider(day = Color(0xFF2571E0), night = Color(0xFF60A5FA))
        }
        Intensity.HIGH -> {
            ColorProvider(day = Color(0xFF1647B0), night = Color(0xFF93C5FD))
        }
        Intensity.MAX -> {
            ColorProvider(day = Color(0xFF0B2556), night = Color(0xFFBFDBFE))
        }
    }
}

private val CardBackgroundProvider: GlanceColor = ColorProvider(
    day = Color(0xFFFFFFFF),
    night = Color(0xFF0F1726),
)

private val InkProvider: GlanceColor = ColorProvider(
    day = Color(0xFF0B1F33),
    night = Color(0xFFF1F5F9),
)

private val MutedInkProvider: GlanceColor = ColorProvider(
    day = Color(0xFF64748B),
    night = Color(0xFF94A3B8),
)

private val BadgeBackgroundProvider: GlanceColor = ColorProvider(
    day = Color(0xFFEBF2FA),
    night = Color(0xFF1A293E),
)

private val BadgeTextProvider: GlanceColor = ColorProvider(
    day = Color(0xFF16427D),
    night = Color(0xFF90CDF4),
)

private val TodayRingProvider: GlanceColor = ColorProvider(
    day = Color(0xFF0B1F33),
    night = Color(0xFF93C5FD),
)

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
