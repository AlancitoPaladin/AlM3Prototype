package com.itsm.prototype.ui.seller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import com.itsm.prototype.R
import com.itsm.prototype.databinding.ActivitySellerBinding
import com.itsm.prototype.model.ModelsAdapter
import com.itsm.prototype.model.ModelsLoadingState
import com.itsm.prototype.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SellerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySellerBinding
    private val viewModel: SellerViewModel by viewModels()

    private lateinit var modelsAdapter: ModelsAdapter
    private var currentUserId: String = ""

    private lateinit var currentPhotoUri: Uri
    private var selectedImageUri: Uri? = null

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            handleImageSelected(it)
        }
    }

    // Fallback GetContent (Android 12-)
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            handleImageSelected(it)
        }
    }

    // Cámara
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri.let {
                selectedImageUri = it
                handleImageSelected(it)
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            showToast("Permiso de cámara denegado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_seller)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupRecyclerView()
        loadUserData()
        observeViewModel()
        setupUI()
    }

    private fun setupRecyclerView() {
        modelsAdapter = ModelsAdapter { model ->
            showToast("Modelo: ${model.name}")
            // TODO: Navegar a pantalla de detalle/edición
        }

        binding.recyclerViewMyModels.apply {
            adapter = modelsAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@SellerActivity)
        }
    }

    private fun loadUserData() {
        val userId = intent.getStringExtra("userId")
        val userEmail = intent.getStringExtra("userEmail")
        val userName = intent.getStringExtra("userName")

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val finalUserId = userId ?: prefs.getString("userId", "") ?: ""
        val finalEmail = userEmail ?: prefs.getString("userEmail", "") ?: ""
        val finalName = userName ?: prefs.getString("userName", "")

        currentUserId = finalUserId

        viewModel.loadSellerData(finalUserId, finalEmail, finalName)
    }

    private fun observeViewModel() {

        viewModel.seller.observe(this) { seller ->
            binding.sellerName = seller.name
            binding.storeName = seller.storeName
        }

        viewModel.processingProgress.observe(this) { progress ->
            binding.progressBarHorizontal.progress = progress
            binding.tvProgressPercent.text = "$progress%"
        }

        viewModel.processingStep.observe(this) { step ->
            binding.tvProgressStep.text = step
        }

        viewModel.myModels.observe(this) { models ->
            modelsAdapter.submitList(models)
        }

        viewModel.modelsLoadingState.observe(this) { state ->
            when (state) {
                is ModelsLoadingState.Loading -> {
                    // Mostrar loading si quieres
                }
                is ModelsLoadingState.Success -> {
                    if (state.count == 0) {
                        showToast("No tienes modelos aún")
                    } else {
                        showToast("${state.count} modelos cargados")
                    }
                }
                is ModelsLoadingState.Error -> {
                    showToast("Error: ${state.message}")
                }
            }
        }

        viewModel.imageProcessingState.observe(this) { state ->
            when (state) {
                is ImageProcessingState.Loading -> {
                    binding.progressCard.visibility = View.VISIBLE
                    binding.btnCreateModel.isEnabled = false
                    // NO mostrar el progressBar circular aquí
                }

                is ImageProcessingState.Success -> {
                    binding.progressCard.visibility = View.GONE
                    binding.btnCreateModel.isEnabled = true

                    showToast("¡Modelo 3D generado!")
                    state.response.detection?.let {
                        showToast("Detectado: ${it.`object`} (${(it.confidence * 100).toInt()}%)")
                    }
                }

                is ImageProcessingState.Error -> {
                    binding.progressCard.visibility = View.GONE
                    binding.btnCreateModel.isEnabled = true
                    showToast("Error: ${state.message}")
                }

                is ImageProcessingState.Idle, null -> {
                    binding.progressCard.visibility = View.GONE
                    binding.btnCreateModel.isEnabled = true
                }
            }
        }

        viewModel.createModelState.observe(this) { state ->
            when (state) {
                is CreateModelState.Success -> {
                    showToast(state.message)
                    viewModel.clearForm()

                    viewModel.refreshModels(currentUserId)
                }
                is CreateModelState.Error -> {
                    showToast(state.message)
                }
            }
        }
    }


    private fun setupUI() {
        // Botón para crear modelo (ahora abre image picker)
        binding.btnCreateModel.setOnClickListener {
            if (validateForm()) {
                showImagePickerDialog()
            }
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun validateForm(): Boolean {
        val name = viewModel.modelName.value ?: ""
        val description = viewModel.modelDescription.value ?: ""
        val price = viewModel.modelPrice.value ?: ""

        return when {
            name.isBlank() -> {
                showToast("Ingresa el nombre del modelo")
                false
            }

            description.isBlank() -> {
                showToast("Ingresa una descripción")
                false
            }

            price.isBlank() -> {
                showToast("Ingresa un precio")
                false
            }

            else -> true
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Cámara", "Galería", "Cancelar")

        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen del producto")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(currentPhotoUri)
    }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ Photo Picker
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            // Android 12- GetContent
            pickImageLauncher.launch("image/*")
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = cacheDir
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun handleImageSelected(uri: Uri) {
        // Mostrar preview de la imagen (opcional)
        // Glide.with(this).load(uri).into(binding.imagePreview)

        viewModel.processImage(this, uri)
    }

    private fun logout() {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        prefs.edit {
            clear()
        }

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}