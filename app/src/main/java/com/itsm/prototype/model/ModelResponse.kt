package com.itsm.prototype.model

class ModelResponse(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val rating: Double,
    val price: Double,
    val category: String,
    val isActive: Boolean = true
)