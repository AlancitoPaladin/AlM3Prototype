package com.itsm.prototype.ui.user

abstract class User(
    private var name: String = "",
    private var email: String = "",
    private var password: String = ""
) {

    fun getName(): String = name
    fun getEmail(): String = email
    fun getPassword(): String = password

    fun setName(name: String) {
        this.name = name
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun setPassword(password: String) {
        this.password = password
    }

    abstract fun getUserType(): String
}