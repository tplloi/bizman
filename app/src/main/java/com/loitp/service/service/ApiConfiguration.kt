package com.loitp.service.service

object ApiConfiguration {
    // authentication
    const val BASE_AUTHEN_URL = "https://tabletopapi.dikauri.com/api/"

    // API Param
    const val AUTHORIZATION_HEADER = "Authorization"
    const val TOKEN_TYPE = "bearer"
    const val TIME_OUT = 20L // In second
    const val LIMIT_PAGE = 20
}
