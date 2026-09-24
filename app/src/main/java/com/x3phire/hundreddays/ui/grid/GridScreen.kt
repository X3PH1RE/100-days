package com.x3phire.hundreddays.ui.grid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.x3phire.hundreddays.domain.ContributionGridModel
import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.DayPhase
import com.x3phire.hundreddays.domain.DaysLeftCorner
import com.x3phire.hundreddays.domain.GridCell
import com.x3phire.hundreddays.domain.Intensity
import com.x3phire.hundreddays.domain.WeekSlot
import com.x3phire.hundreddays.ui.theme.CellBorder
import com.x3phire.hundreddays.ui.theme.CellBorderFuture
import com.x3phire.hundreddays.ui.theme.CellFillFuture
import com.x3phire.hundreddays.ui.theme.TodayRing
import com.x3phire.hundreddays.ui.theme.toBorderColor
import com.x3phire.hundreddays.ui.theme.toCellColor

@Composable
fun GridScreen(
    overview: ContributionGridModel,
    onDayClick: (DayKey) -> Unit,
    onSettings: () -> Unit,
) {
    val weeks = remember(overview.cells) { overview.asWeekColumns() }
    val scroll = rememberScrollState()
    val todayCell = remember(overview.cells) { overview.cells.firstOrNull { it.phase == DayPhase.TODAY } }
    val activeDaysCount = remember(overview.cells) { overview.cells.count { it.count > 0 } }
    val totalDays = overview.cells.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "100 Days",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Daily Micro-Journal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

        // Lowers content towards the center
        Spacer(modifier = Modifier.weight(0.12f))

        // Hero Stats Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatPill(
                label = "Days Left",
                value = "${overview.daysLeft}",
                modifier = Modifier.weight(1f),
            )
            StatPill(
                label = "Active Days",
                value = "$activeDaysCount",
                modifier = Modifier.weight(1f),
            )
            StatPill(
                label = "Total Days",
                value = "$totalDays",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // The Main Contribution Graph Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.90f),
            ),
            border = BorderStroke(1.dp, Color(0xFFD6E2EE)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "JOURNAL MATRIX",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = daysLeftCopy(overview.daysLeft),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val totalWidth = maxWidth
                    val gutterWidth = 14.dp
                    val gutterGap = 8.dp
                    val availableGridWidth = totalWidth - gutterWidth - gutterGap
                    val weekCount = weeks.size.coerceAtLeast(1)
                    val gap = 3.dp

                    val cellSize = ((availableGridWidth - (gap * (weekCount - 1))) / weekCount).coerceIn(13.dp, 18.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scroll),
                        verticalAlignment = Alignment.Top,
                    ) {
                        WeekdayGutter(cellSize = cellSize, gap = gap)
                        Spacer(modifier = Modifier.width(gutterGap))
                        weeks.forEachIndexed { index, week ->
                            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                                week.slots.forEach { slot ->
                                    when (slot) {
                                        WeekSlot.Padding -> {
                                            Box(modifier = Modifier.size(cellSize))
                                        }
                                        is WeekSlot.Day -> {
                                            DayCell(
                                                cell = slot.cell,
                                                size = cellSize,
                                                onClick = { onDayClick(slot.cell.day) },
                                            )
                                        }
                                    }
                                }
                            }
                            if (index != weeks.lastIndex) {
                                Spacer(modifier = Modifier.width(gap))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Future days read-only",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                    IntensityLegend()
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Today's Quick Action Card
        if (todayCell != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDayClick(todayCell.day) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.90f),
                ),
                border = BorderStroke(1.dp, Color(0xFFD6E2EE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(todayCell.intensity.toCellColor())
                                .border(1.5.dp, TodayRing, RoundedCornerShape(3.dp)),
                        )
                        Column {
                            Text(
                                text = "Today's Journal",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (todayCell.count == 0) "No entries yet · Tap to write" else "${todayCell.count} notes recorded today",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open today",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        // Pushes content up slightly to create centered balance
        Spacer(modifier = Modifier.weight(0.25f))

        Text(
            text = badgeCornerHint(overview.corner),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        border = BorderStroke(1.dp, Color(0xFFD6E2EE)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeekdayGutter(cellSize: Dp, gap: Dp) {
    val labels = listOf("M", "", "W", "", "F", "", "")
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        labels.forEach { label ->
            Box(
                modifier = Modifier.size(width = 14.dp, height = cellSize),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (label.isNotEmpty()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
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
    size: Dp,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(2.5.dp)
    val isToday = cell.phase == DayPhase.TODAY
    val isFuture = cell.phase == DayPhase.FUTURE

    val fill = when {
        isFuture && cell.intensity == Intensity.EMPTY -> CellFillFuture
        else -> cell.intensity.toCellColor()
    }
    val border = when {
        isToday -> TodayRing
        isFuture && cell.intensity == Intensity.EMPTY -> CellBorderFuture
        else -> cell.intensity.toBorderColor()
    }
    val borderWidth = if (isToday) 1.2.dp else 0.8.dp

    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(fill, shape)
            .border(borderWidth, border, shape)
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
                    .background(intensity.toCellColor())
                    .border(0.8.dp, intensity.toBorderColor(), RoundedCornerShape(2.dp)),
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
