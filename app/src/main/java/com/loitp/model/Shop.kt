package com.loitp.model

import androidx.annotation.Keep

@Keep
data class Shop(
    val UserInfo: UserInfo,
    val UserRole: UserRole,
    val __type: String,
    val address: String,
    val avatar: String,
    val className: String,
    val companyName: String,
    val country: String,
    val createdAt: String,
    val firstName: String,
    val info: Info,
    val lastName: String,
    val objectId: String,
    val phone: String,
    val requiredPayment: Boolean,
    val role: Role,
    val updatedAt: String,
    val useAds: Boolean,
    val useOrder: Boolean,
    val userEmail: String,
    val username: String
)