package com.itsm.prototype.model

import com.google.gson.annotations.SerializedName

class ModelResponse(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val rating: Double,
    val price: Double,
    val category: String,
    val isActive: Boolean = true
)