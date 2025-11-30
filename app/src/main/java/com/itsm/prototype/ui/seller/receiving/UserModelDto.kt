package com.itsm.prototype.ui.seller.receiving

data class UserModelDto (
    val _id: String,
    val name: String,
    val description: String,
    val category: String,
    val imageUrl: String?,
    val modelUrl: String,
    val modelKey: String,
    val modelFilename: String,
    val price: Double,
    val rating: Double,
    val isActive: Boolean,
    val detectionData: DetectionData?,
    val createdAt: String?,
    val status: String
)