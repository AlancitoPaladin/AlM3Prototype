package com.itsm.prototype.ui.client

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import com.itsm.prototype.R
import com.itsm.prototype.databinding.ActivityClientBinding
import com.itsm.prototype.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientBinding
    private val viewModel: ClientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_client)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        loadUserData()
        observeViewModel()
        setupUI()
    }

    private fun loadUserData() {
        // Intentar obtener de Intent primero
        val userId = intent.getStringExtra("userId")
        val userEmail = intent.getStringExtra("userEmail")
        val userName = intent.getStringExtra("userName")

        // Si no está en Intent, buscar en SharedPreferences
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val finalUserId = userId ?: prefs.getString("userId", "")
        val finalEmail = userEmail ?: prefs.getString("userEmail", "")
        val finalName = userName ?: prefs.getString("userName", "")

        viewModel.loadClientData(finalUserId ?: "", finalEmail ?: "", finalName)
    }

    private fun observeViewModel() {
        viewModel.client.observe(this) { client ->
            // Actualizar UI con datos del cliente
            binding.clientName = client.name
            // Si tienes más campos en el layout, actualízalos aquí
        }

        viewModel.profileState.observe(this) { state ->
            when (state) {
                is ProfileState.Loading -> {
                    // Mostrar loading si es necesario
                }
                is ProfileState.Success -> {
                    showToast("Perfil cargado")
                }
                is ProfileState.Error -> {
                    showToast("Error al cargar perfil: ${state.message}")
                }
                null -> {}
            }
        }

        // Observar catálogo de modelos
        viewModel.models.observe(this) { models ->
            // TODO: Actualizar RecyclerView con modelos
        }
    }

    private fun setupUI() {
        binding.btnLogout.setOnClickListener {
            logout()
        }
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