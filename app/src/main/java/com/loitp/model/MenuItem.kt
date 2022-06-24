package com.loitp.model

import androidx.annotation.Keep

@Keep
data class MenuItem(
    val active: Boolean,
    val categories: List<Category>,
    val createdAt: String,
    val deleted: Boolean,
    val description: String,
    val extras: List<Any>,
    val images: List<Image>,
    val ingredientWarnings: List<Any>,
    val isSinglePrice: Boolean,
    val labels: List<Any>,
    val objectId: String,
    val optionSets: List<Any>,
    val owner: Owner,
    val price: Int,
    val sku: Any,
    val title: String,
    val updatedAt: String
)