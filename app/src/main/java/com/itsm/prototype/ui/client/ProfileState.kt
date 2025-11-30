package com.itsm.prototype.ui.client

sealed class ProfileState {
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}