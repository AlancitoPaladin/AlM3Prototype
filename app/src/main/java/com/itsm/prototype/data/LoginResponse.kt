package com.itsm.prototype.data

data class LoginResponse (
    val success: Boolean,
    val userType: String,
    val email: String,
    val message: String
    )