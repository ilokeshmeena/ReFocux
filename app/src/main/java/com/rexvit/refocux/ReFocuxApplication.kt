package com.rexvit.refocux

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class ReFocuxApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize logging (optional)

        // Other app-wide initializations can go here
        // Example: Firebase, Analytics, Crash Reporting, etc.
    }
}