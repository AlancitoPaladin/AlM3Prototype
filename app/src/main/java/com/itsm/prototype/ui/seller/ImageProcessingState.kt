package com.itsm.prototype.ui.seller

sealed class ImageProcessingState {
    object Loading : ImageProcessingState()
    data class Success(val response: ProcessImageResponse) : ImageProcessingState()
    data class Error(val message: String) : ImageProcessingState()
    object Idle : ImageProcessingState()
}