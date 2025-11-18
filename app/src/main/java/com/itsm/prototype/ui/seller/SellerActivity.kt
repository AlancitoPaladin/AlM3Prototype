package com.itsm.prototype.ui.seller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.itsm.prototype.R
import com.itsm.prototype.databinding.ActivitySellerBinding
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

    private lateinit var currentPhotoUri: Uri
    private var selectedImageUri: Uri? = null

    // Photo Picker (Android 13+)
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

    // Permiso de cámara
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

        loadUserData()
        observeViewModel()
        setupUI()
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val email = prefs.getString("userEmail", "") ?: ""
        viewModel.loadSellerData(email)
    }

    private fun observeViewModel() {
        viewModel.seller.observe(this) { seller ->
            binding.sellerName = seller.name
            binding.storeName = seller.storeName
        }

        viewModel.myModels.observe(this) { models ->
            // TODO: Actualizar RecyclerView con modelos
        }

        viewModel.createModelState.observe(this) { state ->
            when (state) {
                is CreateModelState.Success -> {
                    showToast(state.message)
                    // Limpiar campos después de crear
                    viewModel.clearForm()
                }

                is CreateModelState.Error -> {
                    showToast(state.message)
                }
            }
        }

        viewModel.imageProcessingState.observe(this) { state ->
            when (state) {
                is ImageProcessingState.Loading -> {
                    showToast("Procesando imagen...")
                }

                is ImageProcessingState.Success -> {
                    showToast("Modelo 3D generado exitosamente")
                    val detection = state.response.detection
                    showToast("Objeto detectado: ${detection.`object`} (${detection.confidence})")
                }

                is ImageProcessingState.Error -> {
                    showToast("Error: ${state.message}")
                }

                is ImageProcessingState.Idle -> {
                }

                null -> {
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