package com.vltvplus

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VLTVApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: VLTVApplication
            private set
    }
}

