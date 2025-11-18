package com.itsm.prototype.ui.seller

data class Detection (
    val `object`: String,
    val confidence: Double,
    val bbox: BoundingBox,
    val colors: List<ColorInfo>
)