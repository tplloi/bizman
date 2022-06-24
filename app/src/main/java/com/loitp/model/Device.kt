package com.loitp.model

import androidx.annotation.Keep

@Keep
data class Device(
    val __type: String,
    val appVersion: String,
    val className: String,
    val createdAt: String,
    val deleted: Boolean,
    val deviceId: String,
    val isActive: Boolean,
    val isAllowCallService: Boolean,
    val isAllowShowSystemCampaign: Boolean,
    val isDualScreenDevice: Boolean,
    val isOnline: Boolean,
    val isPrivated: Boolean,
    val location: Location,
    val name: String,
    val nameSearch: String,
    val objectId: String,
    val orientation: String,
    val owner: Owner,
    val position: Position,
    val updatedAt: String
)