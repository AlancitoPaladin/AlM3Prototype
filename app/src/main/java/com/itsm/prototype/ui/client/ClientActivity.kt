package com.itsm.prototype.ui.client

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.itsm.prototype.R
import com.itsm.prototype.databinding.ActivityClientBinding
import com.itsm.prototype.ui.login.LoginActivity

class ClientActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClientBinding
    private val viewModel: ClientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_client)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupWindowInsets()
        loadUserData()
        observeViewModel()
        setupUI()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val email = prefs.getString("userEmail", "") ?: ""
        viewModel.loadClientData(email)
    }

    private fun observeViewModel() {
        // Observe client data
        viewModel.client.observe(this) { client ->
            binding.clientName = client.getName()
        }

        // Observe models list
        viewModel.models.observe(this) { models ->
            // TODO: Setup RecyclerView adapter
            // Example:
            // val adapter = ModelsAdapter(models) { model ->
            //     viewModel.buyModel(model.id, model.price)
            // }
            // binding.recyclerViewModels.adapter = adapter
        }

        // Observe purchase state
        viewModel.purchaseState.observe(this) { state ->
            when (state) {
                is PurchaseState.Success -> {
                    showToast(state.message)
                }

                is PurchaseState.Error -> {
                    showToast(state.message)
                }
            }
        }
    }

    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Panel del Cliente"

        // Setup logout button
        binding.btnLogout.setOnClickListener {
            logout()
        }

        // TODO: Setup RecyclerView
        // binding.recyclerViewModels.layoutManager = LinearLayoutManager(this)
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