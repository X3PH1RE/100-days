package com.x3phire.hundreddays

import android.app.Application
import com.x3phire.hundreddays.di.AppContainer

class HundredDaysApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
