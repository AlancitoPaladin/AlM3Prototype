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
class RegisterViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    val name = MutableLiveData("")
    val lastName = MutableLiveData("")
    val secondName = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val confirmPassword = MutableLiveData("")
    val role = MutableLiveData("")

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun onRegisterClicked() {
        val nameValue = name.value.orEmpty()
        val lastNameValue = lastName.value.orEmpty()
        val secondNameValue = secondName.value.orEmpty().takeIf { it.isNotBlank() }
        val emailValue = email.value.orEmpty()
        val passwordValue = password.value.orEmpty()
        val confirmPasswordValue = confirmPassword.value.orEmpty()
        val roleValue = role.value.orEmpty()

        if (!validateInput(
                nameValue,
                lastNameValue,
                emailValue,
                passwordValue,
                confirmPasswordValue,
                roleValue
            )
        ) return

        _isLoading.value = true
        performRegister(
            nameValue,
            lastNameValue,
            secondNameValue,
            emailValue,
            passwordValue,
            roleValue
        )
    }

    private fun validateInput(
        name: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String
    ): Boolean {
        when {
            name.isBlank() -> {
                _errorMessage.value = "Ingresa tu nombre"
                return false
            }

            lastName.isBlank() -> {
                _errorMessage.value = "Ingresa tu apellido"
                return false
            }

            email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                _errorMessage.value = "Completa todos los campos obligatorios"
                return false
            }

            !email.contains("@") -> {
                _errorMessage.value = "Ingresa un email válido"
                return false
            }

            password.length < 6 -> {
                _errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
                return false
            }

            password != confirmPassword -> {
                _errorMessage.value = "Las contraseñas no coinciden"
                return false
            }

            role.isBlank() -> {
                _errorMessage.value = "Selecciona un rol"
                return false
            }
        }
        return true
    }

    private fun performRegister(
        name: String,
        lastName: String,
        secondName: String?,
        email: String,
        password: String,
        role: String
    ) {
        viewModelScope.launch {
            repository.register(name, lastName, secondName, email, password, role)
                .onSuccess { response ->
                    _isLoading.value = false
                    _registerState.value = RegisterState.Success(response.message)
                }
                .onFailure { error ->
                    _isLoading.value = false
                    _registerState.value = RegisterState.Error(
                        error.message ?: "Error desconocido"
                    )
                }
        }
    }
}