package com.x3phire.hundreddays.data

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.x3phire.hundreddays.domain.GridInvalidator
import com.x3phire.hundreddays.widget.ContributionGridWidget

internal class GlanceGridInvalidator(
    private val context: Context,
) : GridInvalidator {
    override suspend fun invalidate() {
        ContributionGridWidget().updateAll(context.applicationContext)
    }
}
