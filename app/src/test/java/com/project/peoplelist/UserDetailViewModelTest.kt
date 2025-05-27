package com.project.peoplelist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.project.core.User
import com.project.core.Address
import com.project.core.Company
import com.project.core.Geo
import com.project.network.extensions.ApiResult
import com.project.peoplelist.domain.usecase.GetUserByIdUseCase
import com.project.peoplelist.presentation.viewmodel.UserDetailViewModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class UserDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val getUserByIdUseCase = mockk<GetUserByIdUseCase>()
    private val userObserver = mockk<Observer<User?>>(relaxed = true)
    private val isLoadingObserver = mockk<Observer<Boolean>>(relaxed = true)
    private val errorObserver = mockk<Observer<ApiResult.Error<User>?>>(relaxed = true)
    private val cacheMessageObserver = mockk<Observer<String?>>(relaxed = true)

    private lateinit var viewModel: UserDetailViewModel

    private val mockUser = User(
        id = 1,
        name = "John Doe",
        username = "johndoe",
        email = "john@example.com",
        phone = "123-456-7890",
        website = "johndoe.com",
        address = Address(
            street = "Main St",
            suite = "Apt 1",
            city = "New York",
            zipcode = "10001",
            geo = Geo(lat = "40.7128", lng = "-74.0060")
        ),
        company = Company(
            name = "Tech Corp",
            catchPhrase = "Innovation first",
            bs = "technology solutions"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUser success should update user data`() = runTest {
        coEvery { getUserByIdUseCase(1) } returns ApiResult.Success(mockUser)

        viewModel = UserDetailViewModel(1, getUserByIdUseCase)
        viewModel.user.observeForever(userObserver)
        viewModel.isLoading.observeForever(isLoadingObserver)

        testDispatcher.scheduler.advanceUntilIdle()

        verify { isLoadingObserver.onChanged(true) }
        verify { userObserver.onChanged(mockUser) }
        verify { isLoadingObserver.onChanged(false) }
    }

    @Test
    fun `loadUser error should update error state`() = runTest {
        val errorMessage = "Network error"
        coEvery { getUserByIdUseCase(1) } returns ApiResult.Error(errorMessage)

        viewModel = UserDetailViewModel(1, getUserByIdUseCase)
        viewModel.error.observeForever(errorObserver)
        viewModel.isLoading.observeForever(isLoadingObserver)

        testDispatcher.scheduler.advanceUntilIdle()

        verify { isLoadingObserver.onChanged(true) }
        verify { errorObserver.onChanged(any<ApiResult.Error<User>>()) }
        verify { isLoadingObserver.onChanged(false) }
    }

    @Test
    fun `loadUser cache result should update user and cache message`() = runTest {
        val cacheMessage = "Dados do cache local"
        coEvery { getUserByIdUseCase(1) } returns ApiResult.CacheResult(mockUser, cacheMessage)

        viewModel = UserDetailViewModel(1, getUserByIdUseCase)
        viewModel.user.observeForever(userObserver)
        viewModel.cacheMessage.observeForever(cacheMessageObserver)
        viewModel.isLoading.observeForever(isLoadingObserver)

        testDispatcher.scheduler.advanceUntilIdle()

        verify { isLoadingObserver.onChanged(true) }
        verify { userObserver.onChanged(mockUser) }
        verify { cacheMessageObserver.onChanged(cacheMessage) }
        verify { isLoadingObserver.onChanged(false) }
    }

    @Test
    fun `retry should reload user data`() = runTest {
        coEvery { getUserByIdUseCase(1) } returns ApiResult.Success(mockUser)

        viewModel = UserDetailViewModel(1, getUserByIdUseCase)
        viewModel.user.observeForever(userObserver)

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 2) { userObserver.onChanged(mockUser) }
    }

    @Test
    fun `clearCacheMessage should clear cache message`() = runTest {
        coEvery { getUserByIdUseCase(1) } returns ApiResult.Success(mockUser)

        viewModel = UserDetailViewModel(1, getUserByIdUseCase)
        viewModel.cacheMessage.observeForever(cacheMessageObserver)

        viewModel.clearCacheMessage()

        verify { cacheMessageObserver.onChanged(null) }
    }
}