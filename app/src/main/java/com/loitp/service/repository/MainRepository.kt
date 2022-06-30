package com.loitp.service.repository

import com.loitp.model.Data
import com.loitp.service.service.ApiService
import com.loitpcore.service.model.ApiResponse
import com.loitpcore.service.repository.BaseRepository

class MainRepository(private val apiService: ApiService) : BaseRepository() {
    suspend fun getBookingDetail(orderId: String): ApiResponse<Data> = makeApiCall {
        apiService.getBookingDetailAsync(orderId).await()
    }
}
