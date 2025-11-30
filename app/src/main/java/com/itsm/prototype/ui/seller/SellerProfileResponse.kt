package com.itsm.prototype.ui.seller

data class SellerProfileResponse (
    val name: String,
    val email: String,
    val storeName: String,
    val storeDescription: String? = null
)