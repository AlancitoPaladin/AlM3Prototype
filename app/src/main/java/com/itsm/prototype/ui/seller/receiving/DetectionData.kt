package com.itsm.prototype.ui.seller.receiving

import com.itsm.prototype.ui.seller.ColorInfo

data class DetectionData (
    val `object`: String,
    val confidence: Double,
    val colors: List<ColorInfo>
)