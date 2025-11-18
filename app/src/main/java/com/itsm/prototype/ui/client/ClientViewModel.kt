package com.itsm.prototype.ui.client

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsm.prototype.data.UserRepository
import com.itsm.prototype.model.Model
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _models = MutableLiveData<List<Model>>()
    val models: LiveData<List<Model>> = _models

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _logoutState = MutableLiveData<Boolean>()
    val logoutState: LiveData<Boolean> = _logoutState

    init {
        loadModels()
    }

    fun loadModels() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getModels()
                .onSuccess { models ->
                    _isLoading.value = false
                    _models.value = models
                }
                .onFailure { error ->
                    _isLoading.value = false
                    _errorMessage.value = error.message ?: "Error al cargar modelos"
                }
        }
    }

    fun onLogoutClicked() {
        _logoutState.value = true
    }

    fun getNewModels(): List<Model> {
        return _models.value?.take(4) ?: emptyList()
    }

    fun getPopularModels(): List<Model> {
        return _models.value?.drop(4)?.take(12) ?: emptyList()
    }

    fun getRecommendedModels(): List<Model> {
        return _models.value?.takeLast(4) ?: emptyList()
    }
}
