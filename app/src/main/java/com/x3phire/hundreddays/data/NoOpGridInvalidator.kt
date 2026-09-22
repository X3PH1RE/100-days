package com.x3phire.hundreddays.data

import com.x3phire.hundreddays.domain.GridInvalidator

internal class NoOpGridInvalidator : GridInvalidator {
    override suspend fun invalidate() = Unit
}
