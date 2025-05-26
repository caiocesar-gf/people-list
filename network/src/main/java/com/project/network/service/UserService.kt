package com.project.network.service

import com.project.network.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @GET("users")
    suspend fun getUsers(
        @Query("q") searchQuery: String? = null
    ): List<UserDto>

    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Int
    ): UserDto
}