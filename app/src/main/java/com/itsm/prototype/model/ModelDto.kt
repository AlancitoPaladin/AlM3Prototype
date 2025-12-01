package com.itsm.prototype.model

import com.google.gson.annotations.SerializedName
import com.itsm.prototype.data.MongoDate
import com.itsm.prototype.ui.seller.receiving.DetectionData
import com.itsm.prototype.ui.user.UserIdWrapper

data class ModelDto (
    @SerializedName("_id")
    val id: String,

    val name: String,
    val description: String,
    val category: String,
    val modelUrl: String,
    val price: Double,
    val rating: Double = 0.0,
    val isActive: Boolean = true,

    val detectionData: DetectionData?,

    @SerializedName("userId")
    val user: UserIdWrapper,

    val createdAt: MongoDate?
)