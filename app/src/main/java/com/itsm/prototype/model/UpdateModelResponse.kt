package com.itsm.prototype.model

import com.itsm.prototype.ui.seller.receiving.UserModelDto

data class UpdateModelResponse (
    val success: Boolean,
    val message: String?,
    val model: UserModelDto?
)