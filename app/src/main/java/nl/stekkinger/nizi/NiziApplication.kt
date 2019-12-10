package nl.stekkinger.nizi

import android.app.Application

class NiziApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: NiziApplication
            private set
    }

}