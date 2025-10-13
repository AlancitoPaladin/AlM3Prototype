package com.itsm.prototype.ui.seller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jakarta.inject.Inject

class SellerViewModel @Inject constructor() : ViewModel() {
    private val _seller = MutableLiveData<Seller>()
    val seller: LiveData<Seller> = _seller

    private val _myModels = MutableLiveData<List<ModelItem>>()
    val myModels: LiveData<List<ModelItem>> = _myModels

    private val _createModelState = MutableLiveData<CreateModelState>()
    val createModelState: LiveData<CreateModelState> = _createModelState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Fields for creating a new model
    val modelName = MutableLiveData("")
    val modelDescription = MutableLiveData("")
    val modelPrice = MutableLiveData("")

    fun loadSellerData(email: String) {
        _isLoading.value = true

        // TODO: Load from repository/API
        val seller = Seller(
            name = "Vendedor Demo",
            email = email,
            password = "",
            storeName = "Mi Tienda 3D"
        )

        _seller.value = seller
        loadMyModels()
    }

    private fun loadMyModels() {
        // TODO: Load from repository/API
        val sellerModels = _seller.value?.getModels()?.map { modelId ->
            ModelItem(modelId, "Modelo $modelId", "Descripción", 29.99)
        } ?: emptyList()

        _myModels.value = sellerModels
        _isLoading.value = false
    }

    fun createModel() {
        val name = modelName.value.orEmpty()
        val description = modelDescription.value.orEmpty()
        val priceStr = modelPrice.value.orEmpty()

        if (!validateModelInput(name, description, priceStr)) {
            return
        }

        _isLoading.value = true

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val currentSeller = _seller.value

        if (currentSeller != null) {
            val modelId = currentSeller.createModel(name, price, description)

            // Refresh the list
            loadMyModels()

            _isLoading.value = false
            _createModelState.value = CreateModelState.Success("¡Modelo creado exitosamente!")

            // Clear fields
            clearModelFields()
        }
    }

    private fun validateModelInput(name: String, description: String, price: String): Boolean {
        when {
            name.isBlank() -> {
                _createModelState.value = CreateModelState.Error("Ingresa el nombre del modelo")
                return false
            }

            description.isBlank() -> {
                _createModelState.value = CreateModelState.Error("Ingresa la descripción")
                return false
            }

            price.isBlank() -> {
                _createModelState.value = CreateModelState.Error("Ingresa el precio")
                return false
            }

            price.toDoubleOrNull() == null || price.toDouble() <= 0 -> {
                _createModelState.value = CreateModelState.Error("Ingresa un precio válido")
                return false
            }
        }
        return true
    }

    private fun clearModelFields() {
        modelName.value = ""
        modelDescription.value = ""
        modelPrice.value = ""
    }

    fun deleteModel(modelId: String) {
        val currentSeller = _seller.value
        if (currentSeller != null) {
            currentSeller.removeModel(modelId)
            loadMyModels()
        }
    }

    fun getTotalSales(): Int {
        return _seller.value?.getTotalSales() ?: 0
    }

    fun getRating(): Double {
        return _seller.value?.getRating() ?: 0.0
    }
}