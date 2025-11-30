package com.itsm.prototype.ui.seller

import com.google.gson.annotations.SerializedName

data class ProcessImageResponse(
    val success: Boolean,
    val message: String?,
    @SerializedName("task_id")
    val taskId: String? = null,
    @SerializedName("status_url")
    val statusUrl: String? = null,
    val detection: Detection? = null,
    @SerializedName("model_url")
    val modelUrl: String? = null,
    @SerializedName("model_key")
    val modelKey: String? = null,
    @SerializedName("model_filename")
    val modelFilename: String? = null,
    @SerializedName("model_id")
    val modelId: String? = null
)