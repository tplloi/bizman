package com.loitp.model

import androidx.annotation.Keep

@Keep
data class PaymentCallbackLog(
    val __type: String,
    val amount: Int,
    val className: String,
    val createdAt: String,
    val currency: String,
    val objectId: String,
    val order: Order,
    val orderId: String,
    val request: Request,
    val status: String,
    val transactionId: String,
    val updatedAt: String
)