package com.itsm.prototype.data

import com.itsm.prototype.api.ApiClient.apiService
import com.itsm.prototype.ui.login.RegisterRequest
import com.itsm.prototype.ui.login.RegisterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.login(LoginRequest(email, password))
            }

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 401) {
                Result.failure(Exception("Correo o contraseña inválida"))
            } else {
                Result.failure(Exception("Error del servidor (${response.code()})"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        lastName: String,
        secondName: String? = null,
        email: String,
        password: String,
        role: String
    ): Result<RegisterResponse> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(
                name = name,
                lastName = lastName,
                secondName = secondName,
                email = email,
                password = password,
                role = role
            )

            val response = apiService.register(request)

            if (response.isSuccessful) {
                val registerResponse = response.body()
                Result.success(registerResponse ?: RegisterResponse("Registro exitoso"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = when (response.code()) {
                    409 -> "Correo ya registrado"
                    400 -> "Datos inválidos"
                    500 -> "Error interno del servidor"
                    else -> errorBody ?: "Error desconocido"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}