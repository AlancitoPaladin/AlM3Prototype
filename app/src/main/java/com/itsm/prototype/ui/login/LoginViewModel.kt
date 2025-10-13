package com.itsm.prototype.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.itsm.prototype.data.UserRepository

class LoginViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    // Two-way data binding for input fields
    val email = MutableLiveData("")
    val password = MutableLiveData("")

    // UI state
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun onLoginClicked() {
        val emailValue = email.value.orEmpty()
        val passwordValue = password.value.orEmpty()

        if (!validateInput(emailValue, passwordValue)) {
            return
        }

        _isLoading.value = true

        // Simulate network delay (remove in production)
        // In real app, call repository here
        performLogin(emailValue, passwordValue)
    }

    private fun validateInput(email: String, password: String): Boolean {
        when {
            email.isBlank() || password.isBlank() -> {
                _errorMessage.value = "Ingresa el email y la contraseña"
                return false
            }

            !email.contains("@") -> {
                _errorMessage.value = "Ingresa un email válido"
                return false
            }
        }
        return true
    }

    private fun performLogin(email: String, password: String) {
        // TODO: Replace with actual API call
        val userType = determineUserType(email)

        _isLoading.value = false
        _loginState.value = LoginState.Success(userType, email)
    }

    private fun determineUserType(email: String): String {
        return if (email.contains("seller", ignoreCase = true) ||
            email.contains("vendedor", ignoreCase = true)
        ) {
            "SELLER"
        } else {
            "CLIENT"
        }
    }
}