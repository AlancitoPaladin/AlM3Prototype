package com.itsm.prototype.model

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.itsm.prototype.R
import com.itsm.prototype.data.UserRole
import com.itsm.prototype.databinding.ActivityModelViewerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ModelViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModelViewerBinding
    private val viewModel: ModelViewerViewModel by viewModels()

    private lateinit var filamentViewer: FilamentModelViewer
    private var userRole: UserRole = UserRole.CLIENT

    companion object {
        const val EXTRA_MODEL_ID = "model_id"
        const val EXTRA_USER_ROLE = "user_role" // "seller" o "client"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_model_viewer)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupUserRole()
        setupFilamentViewer()
        loadModelData()
        observeViewModel()
        setupUI()
    }

    private fun setupUserRole() {
        val role = intent.getStringExtra(EXTRA_USER_ROLE) ?: "client"
        userRole = if (role == "seller") UserRole.SELLER else UserRole.CLIENT

        when (userRole) {
            UserRole.SELLER -> {
                binding.btnEdit.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.VISIBLE
                binding.btnDownload.visibility = View.VISIBLE
                binding.btnBuy.visibility = View.GONE
            }

            UserRole.CLIENT -> {
                binding.btnEdit.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE
                binding.btnDownload.visibility = View.VISIBLE
                binding.btnBuy.visibility = View.VISIBLE
            }
        }
    }

    private fun setupFilamentViewer() {
        filamentViewer = FilamentModelViewer(this, binding.surfaceView)
        lifecycle.addObserver(filamentViewer)
    }

    private fun loadModelData() {
        val modelId = intent.getStringExtra(EXTRA_MODEL_ID)

        if (modelId.isNullOrBlank()) {
            showToast("ID de modelo inválido")
            finish()
            return
        }

        viewModel.loadModelDetails(modelId)
    }

    private fun observeViewModel() {
        viewModel.modelState.observe(this) { state ->
            when (state) {
                is ModelViewerState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentLayout.visibility = View.GONE
                }

                is ModelViewerState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE

                    val model = state.model
                    binding.model = model

                    // Cargar modelo 3D en Filament
                    loadModelInViewer(model.modelUrl)
                }

                is ModelViewerState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showToast("Error: ${state.message}")
                    finish()
                }
            }
        }

        viewModel.actionState.observe(this) { state ->
            when (state) {
                is ModelActionState.Deleting -> {
                    binding.progressOverlay.visibility = View.VISIBLE
                }

                is ModelActionState.DeleteSuccess -> {
                    binding.progressOverlay.visibility = View.GONE
                    showToast("Modelo eliminado exitosamente")
                    finish()
                }

                is ModelActionState.Downloading -> {
                    binding.progressOverlay.visibility = View.VISIBLE
                    binding.tvProgressMessage.text = "Descargando... ${state.progress}%"
                }

                is ModelActionState.DownloadSuccess -> {
                    binding.progressOverlay.visibility = View.GONE
                    showToast("Modelo descargado: ${state.filePath}")
                }

                is ModelActionState.Error -> {
                    binding.progressOverlay.visibility = View.GONE
                    showToast("Error: ${state.message}")
                }

                is ModelActionState.Idle -> {
                    binding.progressOverlay.visibility = View.GONE
                }

                is ModelActionState.Processing -> {
                    binding.progressOverlay.visibility = View.VISIBLE
                    binding.tvProgressMessage.text = "Procesando..."
                }

                is ModelActionState.PurchaseSuccess -> {
                    binding.progressOverlay.visibility = View.GONE
                    showToast("¡Compra exitosa!")
                }
            }
        }
    }

    private fun loadModelInViewer(modelUrl: String) {
        filamentViewer.loadModelFromUrl(modelUrl) { success ->
            if (!success) {
                showToast("Error al cargar el modelo 3D")
            }
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEdit.setOnClickListener {
            viewModel.modelState.value?.let { state ->
                if (state is ModelViewerState.Success) {
                    showEditDialog(state.model)
                }
            }
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnDownload.setOnClickListener {
            viewModel.modelState.value?.let { state ->
                if (state is ModelViewerState.Success) {
                    viewModel.downloadModel(state.model)
                }
            }
        }

        binding.btnBuy.setOnClickListener {
            viewModel.modelState.value?.let { state ->
                if (state is ModelViewerState.Success) {
                    showPurchaseDialog(state.model)
                }
            }
        }

        // Controles de rotación
        binding.btnResetView.setOnClickListener {
            filamentViewer.resetCamera()
        }

        binding.toggleWireframe.setOnCheckedChangeListener { _, isChecked ->
            filamentViewer.setWireframeMode(isChecked)
        }
    }

    private fun showEditDialog(model: ModelDetails) {
        // ⭐ Diálogo simple sin layout personalizado por ahora
        AlertDialog.Builder(this)
            .setTitle("Editar Modelo")
            .setMessage("Edición de modelo\nNombre: ${model.name}\nPrecio: $${model.price}")
            .setPositiveButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Modelo")
            .setMessage("¿Estás seguro de que deseas eliminar este modelo? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                val modelId = intent.getStringExtra(EXTRA_MODEL_ID) ?: return@setPositiveButton
                viewModel.deleteModel(modelId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPurchaseDialog(model: ModelDetails) {
        AlertDialog.Builder(this)
            .setTitle("Comprar Modelo")
            .setMessage("¿Deseas comprar '${model.name}' por $${model.price}?")
            .setPositiveButton("Comprar") { _, _ ->

                val prefs = getSharedPreferences("session", MODE_PRIVATE)
                val userId = prefs.getString("userId", "") ?: ""
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}