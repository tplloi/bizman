package com.loitp.service.repository

import com.loitp.service.service.ApiService
import com.service.model.ApiResponse
import com.service.model.UserTest
import com.service.repository.BaseRepository

class MainRepository(private val apiService: ApiService) : BaseRepository() {
    suspend fun getUserTest(page: Int): ApiResponse<ArrayList<UserTest>> = makeApiCall {
        apiService.getUserTestAsync(page).await()
    }
}
