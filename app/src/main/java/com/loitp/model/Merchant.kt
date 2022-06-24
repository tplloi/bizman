package com.loitp.model

import androidx.annotation.Keep

@Keep
data class Merchant(
    val address: Any,
    val createdAt: String,
    val createdBy: String,
    val deletedAt: Any,
    val deletedBy: Any,
    val description: Any,
    val email: Any,
    val id: String,
    val logo: Any,
    val merchantIdCode: String,
    val merchantName: String,
    val organization: Any,
    val phone: Any,
    val status: Int,
    val updatedAt: String,
    val updatedBy: Any
)