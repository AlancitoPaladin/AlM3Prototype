package com.itsm.prototype.ui.seller

data class BoundingBox (
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,
    val widthPercent: Double,
    val heightPercent: Double
)