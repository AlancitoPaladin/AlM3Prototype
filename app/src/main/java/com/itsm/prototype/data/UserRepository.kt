package com.itsm.prototype.data

import com.itsm.prototype.api.ApiService
import com.itsm.prototype.model.Model
import com.itsm.prototype.ui.login.RegisterRequest
import com.itsm.prototype.ui.login.RegisterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService
) {

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

    suspend fun getModels(): Result<List<Model>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getModels()

            if (response.isSuccessful) {
                val modelResponses = response.body() ?: emptyList()
                // Convertir ModelResponse a Model
                val models = modelResponses.map { modelResponse ->
                    Model(
                        id = modelResponse.id,
                        name = modelResponse.name,
                        description = modelResponse.description,
                        imageUrl = modelResponse.imageUrl,
                        rating = modelResponse.rating,
                        price = modelResponse.price,
                        category = modelResponse.category
                    )
                }
                Result.success(models)
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "No se encontraron modelos"
                    500 -> "Error del servidor"
                    else -> "Error al cargar modelos: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}