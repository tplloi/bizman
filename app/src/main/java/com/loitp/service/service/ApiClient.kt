package com.loitp.service.service

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.loitpcore.restApi.DateTypeDeserializer
import com.loitpcore.restApi.restClient.RestRequestInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.* // ktlint-disable no-wildcard-imports
import java.util.concurrent.TimeUnit

object ApiClient {

    private fun getBaseUrl() = ApiConfiguration.BASE_AUTHEN_URL
    private var restRequestInterceptor: RestRequestInterceptor? = null

    private fun getClient(url: String): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
        return getRetrofit(url, okHttpClient)
    }

    private fun getRetrofit(url: String, builder: OkHttpClient.Builder): Retrofit {

        restRequestInterceptor = RestRequestInterceptor()

        val interceptor = run {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            }
        }

        builder.apply {
            connectTimeout(ApiConfiguration.TIME_OUT, TimeUnit.SECONDS)
            readTimeout(ApiConfiguration.TIME_OUT, TimeUnit.SECONDS)
            writeTimeout(ApiConfiguration.TIME_OUT, TimeUnit.SECONDS)
            // addInterceptor(AuthenticationInterceptor())
            addNetworkInterceptor(interceptor)
            restRequestInterceptor?.let { rri ->
                addInterceptor(rri)
            }
            // retryOnConnectionFailure(false)
        }

        // val moshi = Moshi.Builder()
        //        .build()

        val gson = GsonBuilder()
            .registerTypeAdapter(Date::class.java, DateTypeDeserializer())
            .create()

        return Retrofit.Builder().apply {
            baseUrl(url)
            addCallAdapterFactory(CoroutineCallAdapterFactory())
            // addConverterFactory(MoshiConverterFactory.create(moshi))
            addConverterFactory(GsonConverterFactory.create(gson))
            client(builder.build())
        }.build()
    }

    val apiService = getClient(getBaseUrl()).create(ApiService::class.java)

    fun addHeader(name: String, value: String) {
        restRequestInterceptor?.addHeader(name, value)
    }

    fun addAuthorization(token: String) {
        addHeader(ApiConfiguration.AUTHORIZATION_HEADER, token)
    }

    fun removeAuthorization() {
        removeHeader(ApiConfiguration.AUTHORIZATION_HEADER)
    }

    fun removeHeader(name: String) {
        restRequestInterceptor?.removeHeader(name)
    }

    fun hasHeader(name: String): Boolean {
        val hasHeader = restRequestInterceptor?.hasHeader(name)
        return hasHeader == true
    }
}
