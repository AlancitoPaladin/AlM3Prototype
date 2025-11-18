package com.itsm.prototype.ui.seller

data class ProcessImageResponse (
    val success: Boolean,
    val message: String?,
    val detection: Detection,
    val modelUrl: String,
    val modelKey: String,
    val modelFilename: String
)