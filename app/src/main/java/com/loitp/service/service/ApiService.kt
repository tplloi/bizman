package com.loitp.service.service

import com.loitp.model.Data
import com.service.model.ApiResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("orders/receipt/{orderId}")
    fun getBookingDetailAsync(@Path("orderId") orderId: String):
            Deferred<Response<ApiResponse<Data>>>
}
