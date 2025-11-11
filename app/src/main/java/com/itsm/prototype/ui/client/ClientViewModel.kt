package com.itsm.prototype.ui.client

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.itsm.prototype.model.Model
import javax.inject.Inject

class ClientViewModel @Inject constructor(

) : ViewModel() {
    private val _client = MutableLiveData<Client>()
    val client: LiveData<Client> = _client

    private val _models = MutableLiveData<List<Model>>()
    val models: LiveData<List<Model>> = _models

    private val _purchaseState = MutableLiveData<PurchaseState>()
    val purchaseState: LiveData<PurchaseState> = _purchaseState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadClientData(email: String) {
        _isLoading.value = true

        // TODO: Load from repository/API
        val client = Client(
            name = "Alan",
            email = email,
            password = ""
        )

        _client.value = client
        loadAvailableModels()
    }

    private fun loadAvailableModels() {
        // TODO: Load from repository/API
        val modelsList = listOf(
            Model("1", "Model 3D Silla", "Silla moderna", 29.99),
            Model("2", "Model 3D Mesa", "Mesa de madera", 49.99),
            Model("3", "Model 3D Lámpara", "Lámpara decorativa", 19.99)
        )

        _models.value = modelsList
        _isLoading.value = false
    }

    fun buyModel(modelId: String, price: Double) {
        _isLoading.value = true

        val currentClient = _client.value
        if (currentClient != null) {
            val success = currentClient.buyModel(modelId, price)

            _isLoading.value = false
            _purchaseState.value = if (success) {
                PurchaseState.Success("¡Compra exitosa!")
            } else {
                PurchaseState.Error("Error al comprar el modelo")
            }
        }
    }

    fun getPurchaseHistory(): List<String> {
        return _client.value?.getPurchaseHistory() ?: emptyList()
    }
}