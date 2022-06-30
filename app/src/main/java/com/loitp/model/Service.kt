package com.loitp.model

import androidx.annotation.Keep

@Keep
data class Service(
    val createdAt: String,
    val createdBy: Any,
    val deletedAt: Any,
    val deletedBy: Any,
    val description: Any,
    val id: String,
    val logo: Any,
    val organization: Any,
    val organizationId: Any,
    val serviceCode: String,
    val serviceName: String,
    val status: Int,
    val updatedAt: String,
    val updatedBy: Any,
    val webhookEndpoint: String
)