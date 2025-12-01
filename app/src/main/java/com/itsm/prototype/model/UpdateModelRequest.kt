package com.itsm.prototype.model

data class UpdateModelRequest (
    val name: String,
    val description: String,
    val price: Double,
    val isActive: Boolean
)