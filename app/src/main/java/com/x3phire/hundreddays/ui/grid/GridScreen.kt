package com.x3phire.hundreddays.ui.grid

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.x3phire.hundreddays.domain.ContributionGridModel
import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.DayPhase
import com.x3phire.hundreddays.domain.DaysLeftCorner
import com.x3phire.hundreddays.domain.GridCell
import com.x3phire.hundreddays.domain.Intensity
import com.x3phire.hundreddays.domain.WeekSlot
import com.x3phire.hundreddays.ui.theme.TodayRing
import com.x3phire.hundreddays.ui.theme.toCellColor

@Composable
fun GridScreen(
    overview: ContributionGridModel,
    onDayClick: (DayKey) -> Unit,
    onSettings: () -> Unit,
) {
    val weeks = remember(overview.cells) { overview.asWeekColumns() }
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "100 Days",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = daysLeftCopy(overview.daysLeft),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = "Tap a day to journal. Future days stay read-only.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll),
            verticalAlignment = Alignment.Top,
        ) {
            WeekdayGutter()
            Spacer(modifier = Modifier.width(8.dp))
            weeks.forEachIndexed { index, week ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    week.slots.forEach { slot ->
                        when (slot) {
                            WeekSlot.Padding -> {
                                Box(modifier = Modifier.size(12.dp))
                            }
                            is WeekSlot.Day -> {
                                DayCell(
                                    cell = slot.cell,
                                    onClick = { onDayClick(slot.cell.day) },
                                )
                            }
                        }
                    }
                }
                if (index != weeks.lastIndex) {
                    Spacer(modifier = Modifier.width(3.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        IntensityLegend()
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = badgeCornerHint(overview.corner),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun WeekdayGutter() {
    val labels = listOf("M", "", "W", "", "F", "", "")
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        labels.forEach { label ->
            Box(
                modifier = Modifier.size(width = 12.dp, height = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (label.isNotEmpty()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    cell: GridCell,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(2.dp)
    val targetAlpha = if (cell.phase == DayPhase.FUTURE) 0.4f else 1f
    val alpha by animateFloatAsState(targetAlpha, tween(220), label = "cellAlpha")
    Box(
        modifier = Modifier
            .size(12.dp)
            .alpha(alpha)
            .clip(shape)
            .background(cell.intensity.toCellColor(), shape)
            .then(
                if (cell.phase == DayPhase.TODAY) {
                    Modifier.border(1.dp, TodayRing, shape)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    )
}

@Composable
private fun IntensityLegend() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            "Less",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        listOf(
            Intensity.EMPTY,
            Intensity.LOW,
            Intensity.MEDIUM,
            Intensity.HIGH,
            Intensity.MAX,
        ).forEach { intensity ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(intensity.toCellColor()),
            )
        }
        Text(
            "More",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun daysLeftCopy(daysLeft: Int): String =
    when (daysLeft) {
        0 -> "Challenge complete"
        1 -> "1 day left"
        else -> "$daysLeft days left"
    }

private fun badgeCornerHint(corner: DaysLeftCorner): String =
    when (corner) {
        DaysLeftCorner.TOP_START -> "Widget badge: top left"
        DaysLeftCorner.TOP_END -> "Widget badge: top right"
    }
