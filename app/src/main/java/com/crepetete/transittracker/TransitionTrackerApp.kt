package com.crepetete.transittracker

import android.app.Application
import android.util.Log
import com.crepetete.transittracker.config.CrashReportingLibrary
import timber.log.Timber

class TransitionTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }
}

/** A tree which logs important information for crash reporting. */
private class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        if (t != null) {
            if (priority == Log.ERROR) {
                CrashReportingLibrary.logError(t)
            } else if (priority == Log.WARN) {
                CrashReportingLibrary.logWarning(t)
            }
        }
    }
}