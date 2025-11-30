package com.itsm.prototype.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsm.prototype.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    val email = MutableLiveData("")
    val password = MutableLiveData("")

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun onLoginClicked() {
        val emailValue = email.value.orEmpty()
        val passwordValue = password.value.orEmpty()

        if (!validateInput(emailValue, passwordValue)) return

        _isLoading.value = true
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
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            repository.login(email, password)
                .onSuccess { response ->
                    _isLoading.value = false

                    val user = response.user

                    val userType = when (user.role.lowercase()) {
                        "vendedor" -> "SELLER"
                        "cliente", "client" -> "CLIENT"
                        else -> "CLIENT"
                    }

                    _loginState.value = LoginState.Success(
                        userType = userType,
                        email = user.email,
                        userId = user.id,
                        userName = user.name
                    )
                }
                .onFailure { error ->
                    _isLoading.value = false
                    _loginState.value = LoginState.Error(error.message ?: "Error desconocido")
                }
        }
    }
}
