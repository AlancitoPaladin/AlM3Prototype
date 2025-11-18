package com.itsm.prototype.ui.seller.creation

data class AvailableModelsResponse (
    val success: Boolean,
    val models: List<String>,
    val count: Int
)