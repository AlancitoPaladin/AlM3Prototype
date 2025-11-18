package com.itsm.prototype.ui.seller.creation

import com.itsm.prototype.ui.seller.ColorInfo

data class CreateModelRequest (
    val name: String,
    val description: String,
    val price: Double,
    val detectedObject: String,
    val confidence: Double,
    val colors: List<ColorInfo>,
    val modelUrl: String,
    val modelKey: String
)