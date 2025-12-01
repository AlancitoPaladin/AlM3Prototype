package com.itsm.prototype.ui.user

import com.google.gson.annotations.SerializedName

data class UserIdWrapper (
    @SerializedName("\$oid")
    val id: String
)