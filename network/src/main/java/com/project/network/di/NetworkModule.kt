package com.project.network.di

import com.project.network.BuildConfig
import com.project.network.datasource.RemoteUserDataSource
import com.project.network.datasource.RemoteUserDataSourceImpl
import com.project.network.service.UserService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    fun load(): List<Module> {
        return listOf(module {
            single {
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            }
            single {
                OkHttpClient.Builder()
                    .addInterceptor(get<HttpLoggingInterceptor>())
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
            }
            single {
                Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(get())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            single<UserService> {
                get<Retrofit>().create(UserService::class.java)
            }
            single<RemoteUserDataSource> {
                RemoteUserDataSourceImpl(get())
            }
        })
    }
}
