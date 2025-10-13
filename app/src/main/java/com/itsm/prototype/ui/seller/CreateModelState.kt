package com.itsm.prototype.ui.seller

sealed class CreateModelState {
    data class Success(val message: String) : CreateModelState()
    data class Error(val message: String) : CreateModelState()
}