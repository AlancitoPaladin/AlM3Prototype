package com.itsm.prototype.data

import com.itsm.prototype.ui.client.Client
import com.itsm.prototype.ui.seller.Seller
import kotlinx.coroutines.delay

class UserRepository {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        // Simulate network delay
        delay(1000)

        // TODO: Replace with actual API call
        val userType = if (email.contains("seller", ignoreCase = true)) {
            "SELLER"
        } else {
            "CLIENT"
        }

        return Result.success(
            LoginResponse(
                success = true,
                userType = userType,
                email = email,
                message = "Login exitoso"
            )
        )
    }

    suspend fun getClientData(email: String): Result<Client> {
        delay(500)

        // TODO: Replace with actual API call
        val client = Client(
            name = "Cliente Demo",
            email = email,
            password = ""
        )

        return Result.success(client)
    }

    suspend fun getSellerData(email: String): Result<Seller> {
        delay(500)

        // TODO: Replace with actual API call
        val seller = Seller(
            name = "Vendedor Demo",
            email = email,
            password = "",
            storeName = "Mi Tienda 3D"
        )

        return Result.success(seller)
    }

    suspend fun getAvailableModels(): Result<List<ModelData>> {
        delay(500)

        // TODO: Replace with actual API call
        val models = listOf(
            ModelData("1", "Model 3D Silla", "Silla moderna", 29.99, ""),
            ModelData("2", "Model 3D Mesa", "Mesa de madera", 49.99, ""),
            ModelData("3", "Model 3D Lámpara", "Lámpara decorativa", 19.99, "")
        )

        return Result.success(models)
    }

    suspend fun createModel(
        name: String,
        description: String,
        price: Double,
        sellerId: String
    ): Result<ModelData> {
        delay(1000)

        // TODO: Replace with actual API call
        val model = ModelData(
            id = "MODEL_${System.currentTimeMillis()}",
            name = name,
            description = description,
            price = price,
            imageUrl = ""
        )

        return Result.success(model)
    }
}