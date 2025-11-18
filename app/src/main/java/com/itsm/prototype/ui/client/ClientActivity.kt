package com.itsm.prototype.ui.client

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.itsm.prototype.R
import com.itsm.prototype.databinding.ActivityClientBinding
import com.itsm.prototype.model.Model
import com.itsm.prototype.model.ModelDetailActivity
import com.itsm.prototype.model.ModelsAdapter
import com.itsm.prototype.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.function.Consumer

@AndroidEntryPoint
class ClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientBinding
    private val viewModel: ClientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_client)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.clientName = getUserNameFromIntent()

        observeViewModel()
        setupRecyclerView()
    }

    private fun observeViewModel() {
        viewModel.models.observe(this) { models ->
            (binding.recyclerViewModels.adapter as? ModelsAdapter)?.submitList(models)
        }

        viewModel.models.observe(this) { models ->
            (binding.recyclerViewModels.adapter as? ModelsAdapter)?.submitList(models)
        }

        viewModel.logoutState.observe(this) { shouldLogout ->
            if (shouldLogout) {
                performLogout()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                showToast(it)
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupRecyclerView() {
        val adapter = ModelsAdapter(Consumer { model ->
            onModelClicked(model)
        })
        binding.recyclerViewModels.adapter = adapter
        binding.recyclerViewModels.layoutManager = GridLayoutManager(this, 2)

        binding.recyclerViewModels.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun onModelClicked(model: Model) {
        showToast("Modelo seleccionado: ${model.name}")

        val intent = Intent(this, ModelDetailActivity::class.java).apply {
            putExtra("MODEL_ID", model.id)
        }
        startActivity(intent)
    }

    private fun getUserNameFromIntent(): String {
        return intent.getStringExtra("USER_NAME") ?: "Cliente"
    }

    private fun performLogout() {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        prefs.edit { clear() }

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}