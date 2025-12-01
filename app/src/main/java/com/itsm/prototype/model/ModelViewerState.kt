package com.itsm.prototype.model

sealed class ModelViewerState {
    object Loading : ModelViewerState()
    data class Success(val model: ModelDetails) : ModelViewerState()
    data class Error(val message: String) : ModelViewerState()
}