package com.x3phire.hundreddays.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.x3phire.hundreddays.domain.ContributionGridModel
import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.DayPhase
import com.x3phire.hundreddays.domain.DaysLeftCorner
import com.x3phire.hundreddays.domain.GridCell
import com.x3phire.hundreddays.ui.theme.toCellColor

@Composable
fun GridScreen(
    overview: ContributionGridModel,
    onDayClick: (DayKey) -> Unit,
    onSettings: () -> Unit,
) {
    val columns = overview.settings.layout.columns
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("100 Days", style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "Settings")
            }
        }

        DaysLeftBadge(
            daysLeft = overview.daysLeft,
            corner = overview.corner,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(overview.cells, key = { it.day.value.toString() }) { cell ->
                GridCellView(
                    cell = cell,
                    onClick = { onDayClick(cell.day) },
                )
            }
        }
    }
}

@Composable
private fun DaysLeftBadge(daysLeft: Int, corner: DaysLeftCorner) {
    val alignment = when (corner) {
        DaysLeftCorner.TOP_START -> Alignment.CenterStart
        DaysLeftCorner.TOP_END -> Alignment.CenterEnd
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = alignment,
    ) {
        Text(
            text = if (daysLeft == 0) "Complete" else "$daysLeft days left",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun GridCellView(
    cell: GridCell,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .alpha(if (cell.phase == DayPhase.FUTURE) 0.45f else 1f)
            .background(cell.intensity.toCellColor(), shape)
            .border(
                width = if (cell.phase == DayPhase.TODAY) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = cell.day.value.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}
