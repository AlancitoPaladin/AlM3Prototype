package com.itsm.prototype.ui.seller

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsm.prototype.model.ModelsLoadingState
import com.itsm.prototype.ui.client.ProfileState
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class SellerViewModel @Inject constructor() : ViewModel() {

    private val apiService = com.itsm.prototype.api.ApiClient.apiService

    private val _seller = MutableLiveData<Seller>()
    val seller: LiveData<Seller> = _seller

    private val _myModels = MutableLiveData<List<ModelItem>>()
    val myModels: LiveData<List<ModelItem>> = _myModels

    private val _createModelState = MutableLiveData<CreateModelState>()
    val createModelState: LiveData<CreateModelState> = _createModelState

    private val _modelsLoadingState = MutableLiveData<ModelsLoadingState>()
    val modelsLoadingState: LiveData<ModelsLoadingState> = _modelsLoadingState

    private val _imageProcessingState = MutableLiveData<ImageProcessingState?>()
    val imageProcessingState: LiveData<ImageProcessingState?> = _imageProcessingState

    private val _processingProgress = MutableLiveData(0)
    val processingProgress: LiveData<Int> = _processingProgress

    private val _processingStep = MutableLiveData("")
    val processingStep: LiveData<String> = _processingStep

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _profileState = MutableLiveData<ProfileState?>()
    val profileState: LiveData<ProfileState?> = _profileState

    val modelName = MutableLiveData("")
    val modelDescription = MutableLiveData("")
    val modelPrice = MutableLiveData("")

    fun loadSellerData(userId: String, email: String, name: String?) {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileState.Loading

                if (name.isNullOrBlank() && userId.isNotBlank()) {
                    loadProfileFromApi(userId)
                } else {
                    _seller.value = Seller(
                        name = name ?: "Vendedor",
                        storeName = "Mi Tienda"
                    )
                    _profileState.value = ProfileState.Success
                }


                loadUserModels(userId)

            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(
                    e.message ?: "Error al cargar datos"
                )
            }
        }
    }

    private suspend fun loadUserModels(userId: String) {
        if (userId.isBlank()) {
            _modelsLoadingState.value = ModelsLoadingState.Error("ID de usuario inválido")
            return
        }

        try {
            _modelsLoadingState.value = ModelsLoadingState.Loading

            val response = apiService.getUserModels(userId)

            if (response.isSuccessful && response.body() != null) {
                val userModelsResponse = response.body()!!

                val modelItems = userModelsResponse.models.map { dto ->
                    ModelItem(
                        id = dto._id,
                        name = dto.name,
                        description = dto.description,
                        price = dto.price,
                        modelUrl = dto.modelUrl,
                        detectedObject = dto.detectionData?.`object` ?: dto.category
                    )
                }

                _myModels.value = modelItems
                _modelsLoadingState.value = ModelsLoadingState.Success(modelItems.size)

                android.util.Log.d("SellerViewModel", "Modelos cargados: ${modelItems.size}")

            } else {
                _modelsLoadingState.value = ModelsLoadingState.Error(
                    "Error al cargar modelos: ${response.code()}"
                )
            }

        } catch (e: Exception) {
            android.util.Log.e("SellerViewModel", "Error cargando modelos", e)
            _modelsLoadingState.value = ModelsLoadingState.Error(
                e.message ?: "Error de conexión"
            )
        }
    }

    // ⭐ Función pública para recargar modelos
    fun refreshModels(userId: String) {
        viewModelScope.launch {
            loadUserModels(userId)
        }
    }

    private suspend fun loadProfileFromApi(userId: String) {
        try {
            val response = apiService.getSellerProfile(userId)

            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                _seller.value = Seller(
                    name = profile.name,
                    storeName = profile.storeName
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

    fun processImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            try {
                _imageProcessingState.value = ImageProcessingState.Loading
                _processingProgress.value = 0
                _processingStep.value = "Subiendo imagen..."

                val file = uriToFile(context, imageUri)
                if (file == null) {
                    _imageProcessingState.value =
                        ImageProcessingState.Error("Error al leer la imagen")
                    return@launch
                }

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
                val userId = prefs.getString("userId", "") ?: ""
                val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())

                // ⭐ Enviar con userId
                val response = apiService.processImage(imagePart, userIdBody)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    Log.d(
                        "SellerViewModel",
                        "Response: success=${body.success}, taskId=${body.taskId}"
                    )

                    if (body.success && body.taskId != null) {
                        Log.d(
                            "SellerViewModel",
                            "Iniciando polling para taskId: ${body.taskId}"
                        )
                        pollProcessingStatus(body.taskId)
                    } else {
                        _imageProcessingState.value = ImageProcessingState.Error(
                            body.message ?: "Error al iniciar procesamiento"
                        )
                    }
                } else {
                    _imageProcessingState.value = ImageProcessingState.Error(
                        "Error HTTP ${response.code()}: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                Log.e("SellerViewModel", "Error en processImage", e)
                _imageProcessingState.value = ImageProcessingState.Error(
                    "Error: ${e.message ?: "Error al procesar la imagen"}"
                )
            }
        }
    }

    private suspend fun pollProcessingStatus(taskId: String) {
        try {
            Log.d("SellerViewModel", "Entrando a pollProcessingStatus")

            while (true) {
                delay(2000)

                Log.d(
                    "SellerViewModel",
                    "Consultando estado para taskId: $taskId"
                )

                val statusResponse = apiService.getProcessingStatus(taskId)

                Log.d(
                    "SellerViewModel",
                    "Status response code: ${statusResponse.code()}"
                )

                if (!statusResponse.isSuccessful) {
                    Log.e(
                        "SellerViewModel",
                        "Respuesta HTTP inválida: ${statusResponse.code()}"
                    )
                    _imageProcessingState.value =
                        ImageProcessingState.Error("Error al consultar estado")
                    break
                }

                val status = statusResponse.body()
                if (status == null) {
                    Log.e("SellerViewModel", "Body NULL en respuesta de status")
                    _imageProcessingState.value =
                        ImageProcessingState.Error("Respuesta inválida del servidor")
                    break
                }

                Log.d(
                    "SellerViewModel",
                    "Status: ${status.status}, Progress: ${status.progress}"
                )

                _processingProgress.value = status.progress
                _processingStep.value = status.currentStep ?: "Procesando..."

                when (val currentStatus = status.status) {

                    "completed" -> {
                        Log.d("SellerViewModel", "Proceso completado!")

                        if (status.detection != null && status.modelUrl != null) {
                            val completeResponse = ProcessImageResponse(
                                success = true,
                                message = "Procesamiento completado",
                                taskId = taskId,
                                statusUrl = null,
                                detection = status.detection,
                                modelUrl = status.modelUrl,
                                modelKey = status.modelKey,
                                modelFilename = status.modelFilename
                            )

                            _imageProcessingState.value =
                                ImageProcessingState.Success(completeResponse)

                            createModelWithData(completeResponse)
                        } else {
                            Log.e(
                                "SellerViewModel",
                                "Completed sin datos suficientes: $status"
                            )
                            _imageProcessingState.value =
                                ImageProcessingState.Error("Datos incompletos del servidor")
                        }

                        return
                    }

                    "error" -> {
                        Log.e(
                            "SellerViewModel",
                            "Error en procesamiento: ${status.message}"
                        )

                        _imageProcessingState.value = ImageProcessingState.Error(
                            status.message ?: "Error en el procesamiento"
                        )

                        return
                    }

                    "processing" -> {
                        Log.d(
                            "SellerViewModel",
                            "Aún procesando, continuar polling..."
                        )
                        continue
                    }

                    else -> {
                        Log.e(
                            "SellerViewModel",
                            "Estado desconocido: $currentStatus"
                        )

                        _imageProcessingState.value =
                            ImageProcessingState.Error("Estado desconocido: $currentStatus")

                        return
                    }
                }

            }
        } catch (e: Exception) {
            Log.e("SellerViewModel", "Exception en polling", e)
            _imageProcessingState.value =
                ImageProcessingState.Error("Error en polling: ${e.message}")
        }
    }


    private fun createModelWithData(response: ProcessImageResponse) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val detection = response.detection
                val modelUrl = response.modelUrl
                val modelKey = response.modelKey
                val modelDbId = response.modelId

                if (detection == null || modelUrl == null || modelKey == null) {
                    _createModelState.value = CreateModelState.Error(
                        "Datos incompletos para crear el modelo"
                    )
                    return@launch
                }

                if (modelDbId != null) {
                    try {
                        val updateData = mapOf(
                            "name" to (modelName.value ?: ""),
                            "description" to (modelDescription.value ?: ""),
                            "price" to (modelPrice.value?.toDoubleOrNull() ?: 0.0),
                            "isActive" to true  // Activar el modelo
                        )

                        // TODO: Crear endpoint para actualizar modelo existente
                        // apiService.updateModel(modelDbId, updateData)

                        _createModelState.value = CreateModelState.Success(
                            "Modelo creado y guardado exitosamente"
                        )
                    } catch (e: Exception) {
                        _createModelState.value = CreateModelState.Error(
                            "Modelo generado pero error al actualizar: ${e.message}"
                        )
                    }
                } else {
                    _createModelState.value = CreateModelState.Success(
                        "Modelo generado (sin guardar en BD)"
                    )
                }

            } catch (e: Exception) {
                _createModelState.value = CreateModelState.Error(
                    e.message ?: "Error al procesar modelo"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearForm() {
        modelName.value = ""
        modelDescription.value = ""
        modelPrice.value = ""
        _imageProcessingState.value = ImageProcessingState.Idle
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
