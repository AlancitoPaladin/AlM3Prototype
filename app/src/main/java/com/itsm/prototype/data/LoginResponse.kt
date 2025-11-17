package com.itsm.prototype.data

import com.itsm.prototype.ui.user.User

data class LoginResponse(
    val message: String,
    val user: User
)