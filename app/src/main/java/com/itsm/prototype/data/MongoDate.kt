package com.itsm.prototype.data

import com.google.gson.annotations.SerializedName

data class MongoDate(
    @SerializedName("\$date")
    val date: String
)