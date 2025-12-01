package com.itsm.prototype.model

data class PurchaseRequest (
    val modelId: String,
    val userId: String,
    val paymentMethod: String? = null
)