package com.loitp.model

import androidx.annotation.Keep

@Keep
data class Info(
    val __type: String,
    val className: String,
    val objectId: String
)