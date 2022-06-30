package com.loitp.model

import androidx.annotation.Keep

@Keep
data class Input(
    val note: String,
    val objectId: String,
    val optionSets: List<Any>,
    val quantity: Int
)