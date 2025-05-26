package com.project.peoplelist.di

import com.project.database.di.DatabaseModule
import com.project.network.di.NetworkModule
import com.project.peoplelist.data.UserRepositoryImpl
import com.project.peoplelist.domain.repository.UserRepository
import com.project.peoplelist.domain.usecase.GetUserByIdUseCase
import com.project.peoplelist.domain.usecase.GetUsersUseCase
import com.project.peoplelist.presentation.viewmodel.UserDetailViewModel
import com.project.peoplelist.presentation.viewmodel.UserListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object AppModule {

    fun load(): List<Module> {
        return NetworkModule.load() +
                DatabaseModule.load() +
                featureModules()
    }

    private fun featureModules(): List<Module> {
        return listOf(module {
            single<UserRepository> {
                UserRepositoryImpl(
                    remoteUserDataSource = get(),
                    localUserDataSource = get()
                )
            }

            single {
                GetUsersUseCase(userRepository = get())
            }

            single {
                GetUserByIdUseCase(userRepository = get())
            }

            // ViewModels
            viewModel {
                UserListViewModel(
                    getUsersUseCase = get()
                )
            }

            viewModel { (userId: Int) ->
                UserDetailViewModel(
                    userId = userId,
                    getUserByIdUseCase = get()
                )
            }
        })
    }
}