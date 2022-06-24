package com.loitp.model

import androidx.annotation.Keep

@Keep
data class Request(
    val amount: String,
    val balanceAmount: String,
    val connection: Any,
    val createdAt: String,
    val createdBy: Any,
    val currency: String,
    val deletedAt: Any,
    val deletedBy: Any,
    val description: String,
    val dikauriAmount: String,
    val dikauriFeeAmount: String,
    val errorMessage: Any,
    val gstAmount: String,
    val id: String,
    val ip: String,
    val merchant: Merchant,
    val merchantId: String,
    val merchantIdCode: String,
    val metadata: String,
    val orderId: String,
    val organization: Any,
    val ppAmount: String,
    val provider: String,
    val redirectUrl: String,
    val searchText: String,
    val service: Service,
    val serviceId: String,
    val statementId: Any,
    val status: String,
    val transactionId: String,
    val updatedAt: String,
    val updatedBy: Any,
    val userId: Any
)