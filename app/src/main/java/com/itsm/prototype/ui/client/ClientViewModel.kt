package com.itsm.prototype.ui.client

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsm.prototype.api.ApiClient
import com.itsm.prototype.model.CatalogModelItem
import jakarta.inject.Inject
import kotlinx.coroutines.launch

class ClientViewModel @Inject constructor() : ViewModel() {

    private val apiService = ApiClient.apiService

    private val _client = MutableLiveData<Client>()
    val client: LiveData<Client> = _client

    private val _profileState = MutableLiveData<ProfileState?>()
    val profileState: LiveData<ProfileState?> = _profileState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _models = MutableLiveData<List<CatalogModelItem>>()
    val models: LiveData<List<CatalogModelItem>> = _models


    fun loadClientData(userId: String, email: String, name: String?) {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileState.Loading

                if (name.isNullOrBlank() && userId.isNotBlank()) {
                    loadProfileFromApi(userId)
                } else {
                    _client.value = Client(
                        id = userId,
                        name = name ?: "Cliente",
                        email = email
                    )
                    _profileState.value = ProfileState.Success
                }

                loadCatalog()

            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(
                    e.message ?: "Error al cargar datos"
                )
            }
        }
    }

    private suspend fun loadProfileFromApi(userId: String) {
        try {
            val response = apiService.getClientProfile(userId)

            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                _client.value = Client(
                    id = userId,
                    name = profile.name,
                    email = profile.email
                )
                _profileState.value = ProfileState.Success
            } else {
                _profileState.value = ProfileState.Error(
                    "Error al cargar perfil: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            _profileState.value = ProfileState.Error(
                e.message ?: "Error de conexión"
            )
        }
    }

    private suspend fun loadCatalog() {
        try {
            val response = apiService.getModels()

            if (response.isSuccessful && response.body() != null) {
                _models.value = response.body()!!
            } else {
                Log.e("ClientViewModel", "Error HTTP: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e("ClientViewModel", "Error loading catalog", e)
        }
    }
}