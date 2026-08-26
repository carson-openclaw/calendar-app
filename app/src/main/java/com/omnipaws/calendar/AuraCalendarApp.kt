package com.omnipaws.calendar

import android.app.Application
import com.omnipaws.calendar.data.EventRepository

class AuraCalendarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        EventRepository.init(this)
    }
}
