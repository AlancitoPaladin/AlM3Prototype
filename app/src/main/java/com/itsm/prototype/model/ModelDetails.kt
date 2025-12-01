package com.itsm.prototype.model

import com.google.gson.annotations.SerializedName
import com.itsm.prototype.data.MongoDate
import com.itsm.prototype.ui.seller.receiving.DetectionData
import com.itsm.prototype.ui.user.UserIdWrapper

data class ModelDetails(
    @SerializedName("_id")
    val id: String,

    val name: String,
    val description: String,
    val price: Double,
    val modelUrl: String,
    val category: String,
    val rating: Double,
    val isActive: Boolean,

    val detectionData: DetectionData,

    @SerializedName("userId")
    val user: UserIdWrapper,

    val createdAt: MongoDate
)