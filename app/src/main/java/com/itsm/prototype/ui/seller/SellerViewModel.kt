package com.itsm.prototype.ui.seller

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsm.prototype.ui.seller.creation.CreateModelRequest
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class SellerViewModel @Inject constructor() : ViewModel() {

    private val apiService = com.itsm.prototype.api.ApiClient.apiService

    private val _seller = MutableLiveData<Seller>()
    val seller: LiveData<Seller> = _seller

    private val _myModels = MutableLiveData<List<ModelItem>>()
    val myModels: LiveData<List<ModelItem>> = _myModels

    private val _createModelState = MutableLiveData<CreateModelState>()
    val createModelState: LiveData<CreateModelState> = _createModelState

    private val _imageProcessingState = MutableLiveData<ImageProcessingState?>()
    val imageProcessingState: LiveData<ImageProcessingState?> = _imageProcessingState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val modelName = MutableLiveData("")
    val modelDescription = MutableLiveData("")
    val modelPrice = MutableLiveData("")

    fun loadSellerData(email: String) {
        // TODO: Cargar datos del vendedor desde API
        _seller.value = Seller(
            name = "Juan Pérez",
            storeName = "Mi Tienda"
        )
    }

    fun processImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            try {
                _imageProcessingState.value = ImageProcessingState.Loading

                // Convertir URI a File
                val file = uriToFile(context, imageUri)
                if (file == null) {
                    _imageProcessingState.value =
                        ImageProcessingState.Error("Error al leer la imagen")
                    return@launch
                }

                // Crear MultipartBody.Part
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                // Llamar a la API
                // val response = apiService.processImage(imagePart)

                // SIMULACIÓN (reemplaza con llamada real)
                val response = ProcessImageResponse(
                    success = true,
                    message = "Imagen procesada exitosamente",
                    detection = Detection(
                        `object` = "car",
                        confidence = 0.92,
                        bbox = BoundingBox(120, 80, 450, 380, 45.5, 62.3),
                        colors = listOf(
                            ColorInfo("#c41e3a", 55.2),
                            ColorInfo("#1a1a1a", 30.5)
                        )
                    ),
                    modelUrl = "https://example.com/model.glb",
                    modelKey = "generated_models/123456_car.glb",
                    modelFilename = "123456_car.glb"
                )

                if (response.success) {
                    _imageProcessingState.value = ImageProcessingState.Success(response)

                    // Crear el modelo con los datos obtenidos
                    createModelWithData(response)
                } else {
                    _imageProcessingState.value = ImageProcessingState.Error(
                        response.message ?: "Error desconocido"
                    )
                }

            } catch (e: Exception) {
                _imageProcessingState.value = ImageProcessingState.Error(
                    e.message ?: "Error al procesar la imagen"
                )
            }
        }
    }

    private fun createModelWithData(response: ProcessImageResponse) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Crear modelo con los datos del formulario + datos de la API
                val modelData = CreateModelRequest(
                    name = modelName.value ?: "",
                    description = modelDescription.value ?: "",
                    price = modelPrice.value?.toDoubleOrNull() ?: 0.0,
                    detectedObject = response.detection.`object`,
                    confidence = response.detection.confidence,
                    colors = response.detection.colors,
                    modelUrl = response.modelUrl,
                    modelKey = response.modelKey
                )

                // TODO: Enviar a tu backend para guardar en BD
                // val result = apiService.createModel(modelData)

                _createModelState.value = CreateModelState.Success("Modelo creado exitosamente")

            } catch (e: Exception) {
                _createModelState.value = CreateModelState.Error(
                    e.message ?: "Error al crear modelo"
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
