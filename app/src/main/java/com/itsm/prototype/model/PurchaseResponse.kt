package com.itsm.prototype.model

data class PurchaseResponse (
    val success: Boolean,
    val message: String?,
    val transactionId: String?,
    val downloadUrl: String?
)