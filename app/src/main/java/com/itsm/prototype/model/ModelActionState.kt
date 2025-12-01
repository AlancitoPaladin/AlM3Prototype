package com.itsm.prototype.model

sealed class ModelActionState {
    object Idle : ModelActionState()
    object Deleting : ModelActionState()
    object DeleteSuccess : ModelActionState()
    data class Downloading(val progress: Int) : ModelActionState()
    data class DownloadSuccess(val filePath: String) : ModelActionState()
    object Processing : ModelActionState()
    object PurchaseSuccess : ModelActionState()
    data class Error(val message: String) : ModelActionState()
}