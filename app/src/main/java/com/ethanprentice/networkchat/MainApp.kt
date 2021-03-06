package com.ethanprentice.networkchat

import android.app.Application
import android.content.Context
import com.ethanprentice.networkchat.adt.ShakaActivity
import com.ethanprentice.networkchat.connection_manager.ConnectionManager

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        application = this
        connManager = ConnectionManager()
    }

    override fun onTerminate() {
        connManager.closeAll()
        super.onTerminate()
    }

    companion object {
        var application: Application? = null
            private set

        var currActivity: ShakaActivity? = null

        val context: Context
            get() = application!!.applicationContext


        lateinit var connManager: ConnectionManager
            private set

    }
}