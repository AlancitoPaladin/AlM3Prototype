package com.itsm.prototype.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.itsm.prototype.R
import com.itsm.prototype.databinding.ActivityRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupRoleDropdown()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRoleDropdown() {
        val roles = arrayOf("CLIENT", "SELLER")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.role.setAdapter(adapter)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterState.Success -> {
                    showToast(state.message)
                    navigateToLogin()
                }

                is RegisterState.Error -> {
                    showToast(state.message)
                }
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                showToast(it)
            }
        }
    }

    private fun setupClickListeners() {
        binding.loginRedirect.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}