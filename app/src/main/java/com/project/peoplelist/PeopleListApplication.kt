package com.project.peoplelist

import android.app.Application
import com.project.database.di.DatabaseModule
import com.project.network.di.NetworkModule
import com.project.peoplelist.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class PeopleListApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PeopleListApplication)
            modules(
                AppModule.load()  // ✅ AppModule já inclui Network e Database
            )
        }
    }
}