package com.loitp.model

import androidx.annotation.Keep

@Keep
data class Data(
    val GST: Double,
    val GSTAmount: Double,
    val createdAt: String,
    val device: Device,
    val initPaymentResponse: InitPaymentResponse,
    val input: List<Input>,
    val isPaid: Boolean,
    val isTakeAway: Boolean,
    val items: List<Item>,
    val objectId: String,
    val paymentCallbackLog: PaymentCallbackLog,
    val paymentProvider: String,
    val resolved: Boolean,
    val resolvedBy: ResolvedBy,
    val shop: Shop,
    val status: String,
    val tableName: String,
    val total: Int,
    val transactionId: String,
    val updatedAt: String
)