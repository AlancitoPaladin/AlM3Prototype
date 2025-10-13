package com.itsm.prototype.ui.seller

import com.itsm.prototype.ui.user.User

class Seller(
    name: String = "",
    email: String = "",
    password: String = "",
    private var storeName: String = "",
    private var models: MutableList<String> = mutableListOf(),
    private var rating: Double = 0.0,
    private var totalSales: Int = 0
) : User(name, email, password) {

    override fun getUserType(): String = "SELLER"

    // Métodos específicos para el vendedor
    fun getStoreName(): String = storeName

    fun setStoreName(name: String) {
        this.storeName = name
    }

    fun getModels(): List<String> = models

    fun addModel(modelId: String) {
        models.add(modelId)
    }

    fun removeModel(modelId: String): Boolean {
        return models.remove(modelId)
    }

    fun getTotalModels(): Int = models.size

    fun getRating(): Double = rating

    fun updateRating(newRating: Double) {
        this.rating = newRating
    }

    fun getTotalSales(): Int = totalSales

    fun incrementSales() {
        totalSales++
    }

    // Método para crear un nuevo modelo
    fun createModel(modelName: String, price: Double, description: String): String {
        // Add your model creation logic here
        val modelId = "MODEL_${System.currentTimeMillis()}"
        addModel(modelId)
        return modelId
    }
}