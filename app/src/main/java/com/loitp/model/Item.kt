package com.loitp.model

import androidx.annotation.Keep

@Keep
data class Item(
    val menuItem: MenuItem,
    val note: String,
    val quantity: Int,
    val subTotal: Double,
    val userSelection: UserSelection
)