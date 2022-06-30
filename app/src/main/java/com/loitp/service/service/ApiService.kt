package com.loitp.service.service

import com.loitp.model.Data
import com.loitpcore.service.model.ApiResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("orders/receipt/{orderId}")
    fun getBookingDetailAsync(@Path("orderId") orderId: String):
            Deferred<Response<ApiResponse<Data>>>
}
