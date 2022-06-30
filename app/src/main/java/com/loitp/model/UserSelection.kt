package com.loitp.model

import androidx.annotation.Keep

@Keep
data class UserSelection(
    val note: String,
    val objectId: String,
    val optionSets: List<Any>,
    val quantity: Int
)