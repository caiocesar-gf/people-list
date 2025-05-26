package com.project.database.di

import androidx.room.Room
import com.project.database.UserDatabase
import com.project.database.datasource.LocalUserDataSource
import com.project.database.datasource.LocalUserDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module


object DatabaseModule {

    fun load(): List<Module> {
        return listOf(module {
            single {
                Room.databaseBuilder(
                    androidContext(),
                    UserDatabase::class.java,
                    "user_database"
                ).build()
            }

            single {
                get<UserDatabase>().userDao()
            }

            single<LocalUserDataSource> {
                LocalUserDataSourceImpl(get())
            }
        })
    }
}