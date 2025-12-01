package com.itsm.prototype.model

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.itsm.prototype.api.ApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ModelViewerViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val apiService = ApiClient.apiService

    private val _modelState = MutableLiveData<ModelViewerState>()
    val modelState: LiveData<ModelViewerState> = _modelState

    private val _actionState = MutableLiveData<ModelActionState>()
    val actionState: LiveData<ModelActionState> = _actionState

    fun loadModelDetails(modelId: String) {
        viewModelScope.launch {
            try {
                _modelState.value = ModelViewerState.Loading

                val response = apiService.getModelById(modelId)

                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!.model

                    val modelDetails = ModelDetails(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        price = dto.price,
                        modelUrl = dto.modelUrl,
                        category = dto.category,
                        rating = 0.0,
                        isActive = true,
                        detectionData = dto.detectionData
                            ?: throw IllegalStateException("detectionData no puede ser null"),
                        user = response.body()!!.model.user,
                        createdAt = dto.createdAt
                            ?: throw IllegalStateException("createdAt no puede ser null")
                    )

                    _modelState.value = ModelViewerState.Success(modelDetails)

                } else {
                    _modelState.value =
                        ModelViewerState.Error("Error: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("ModelViewerVM", "Error loading model", e)
                _modelState.value =
                    ModelViewerState.Error(e.message ?: "Error de conexión")
            }
        }
    }



    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            try {
                _actionState.value = ModelActionState.Deleting

                val response = apiService.deleteModel(modelId)

                _actionState.value =
                    if (response.isSuccessful)
                        ModelActionState.DeleteSuccess
                    else
                        ModelActionState.Error("Error: ${response.code()}")

            } catch (e: Exception) {
                Log.e("ModelViewerVM", "Error deleting model", e)
                _actionState.value =
                    ModelActionState.Error(e.message ?: "Error al eliminar")
            }
        }
    }

    fun downloadModel(model: ModelDetails) {
        viewModelScope.launch {
            try {
                _actionState.value = ModelActionState.Downloading(0)

                val response = apiService.downloadModel(model.modelUrl)

                if (response.isSuccessful && response.body() != null) {
                    val file = saveModelToFile(response.body()!!, model.name)
                    _actionState.value =
                        ModelActionState.DownloadSuccess(file.absolutePath)
                } else {
                    _actionState.value =
                        ModelActionState.Error("Error al descargar")
                }

            } catch (e: Exception) {
                Log.e("ModelViewerVM", "Error downloading model", e)
                _actionState.value =
                    ModelActionState.Error(e.message ?: "Error descarga")
            }
        }
    }

    private fun saveModelToFile(body: ResponseBody, modelName: String): File {
        val context = getApplication<Application>()
        val dir = File(context.getExternalFilesDir(null), "Models")
        if (!dir.exists()) dir.mkdirs()

        val file = File(
            dir,
            "${modelName.replace(" ", "_")}_${System.currentTimeMillis()}.glb"
        )

        FileOutputStream(file).use { output ->
            body.byteStream().copyTo(output)
        }

        return file
    }
}
