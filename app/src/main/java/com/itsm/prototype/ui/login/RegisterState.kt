package com.itsm.prototype.ui.login

sealed class RegisterState {
    data class Success(val message: String) : RegisterState()
    data class Error(val message: String) : RegisterState()
}