package com.itsm.prototype.ui.login

data class RegisterRequest (
    val name: String,
    val lastName: String,
    val secondName: String? = null,
    val email: String,
    val password: String,
    val role: String,
    val profilePicture: String? = null,
    val bio: String? = null,
    val isActive: Boolean = true
)