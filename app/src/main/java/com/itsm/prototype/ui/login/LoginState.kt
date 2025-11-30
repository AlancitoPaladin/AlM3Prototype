package com.itsm.prototype.ui.login

sealed class LoginState {
    object Loading : LoginState()
    data class Success(
        val userType: String,
        val email: String,
        val userId: String,
        val userName: String? = null
    ) : LoginState()

    data class Error(val message: String) : LoginState()
}