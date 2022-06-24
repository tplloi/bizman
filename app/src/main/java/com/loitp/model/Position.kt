package com.loitp.model

import androidx.annotation.Keep

@Keep
data class Position(
    val __type: String,
    val latitude: Double,
    val longitude: Double
)