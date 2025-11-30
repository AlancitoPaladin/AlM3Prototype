package com.itsm.prototype.ui.seller.receiving

data class UserModelsResponse (
    val success: Boolean,
    val models: List<UserModelDto>,
    val count: Int
)