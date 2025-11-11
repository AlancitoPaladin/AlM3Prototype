package com.itsm.prototype.ui.login

sealed class LoginState {
    data class Success(val userType: String, val email: String) : LoginState()
    data class Error(val message: String) : LoginState()
}