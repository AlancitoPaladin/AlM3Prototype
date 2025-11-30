package com.itsm.prototype.ui.seller

data class ProcessingStatusResponse(
    val success: Boolean? = null,
    val status: String,
    val progress: Int,
    val currentStep: String? = null,
    val message: String? = null,
    val detection: Detection? = null,
    val modelUrl: String? = null,
    val modelKey: String? = null,
    val modelFilename: String? = null
)