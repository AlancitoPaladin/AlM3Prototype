package com.itsm.prototype.model

sealed class ModelsLoadingState {
    object Loading : ModelsLoadingState()
    data class Success(val count: Int) : ModelsLoadingState()
    data class Error(val message: String) : ModelsLoadingState()
}