package com.loitp.model

import androidx.annotation.Keep

@Keep
data class InitPaymentResponse(
    val `data`: DataX,
    val message: String
)