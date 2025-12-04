package com.itsm.prototype.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import com.itsm.prototype.R
import com.itsm.prototype.databinding.ActivityLoginBinding
import com.itsm.prototype.ui.client.ClientActivity
import com.itsm.prototype.ui.seller.SellerActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Success -> {
                    saveLoginSession(state)
                    showToast("¡Bienvenido!")
                    navigateToAppropriateActivity(state)
                }

                is LoginState.Error -> {
                    showToast(state.message)
                }

                else -> {}
            }
        }
    }

    private fun saveLoginSession(state: LoginState.Success) {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        prefs.edit {
            putBoolean("isLoggedIn", true)
            putString("userType", state.userType)
            putString("userEmail", state.email)
            putString("userId", state.userId)
            state.userName?.let { putString("userName", it) }
        }
    }

    private fun navigateToAppropriateActivity(state: LoginState.Success) {
        val intent = when (state.userType) {
            "SELLER" -> Intent(this, SellerActivity::class.java).apply {
                putExtra("userId", state.userId)
                putExtra("userEmail", state.email)
                state.userName?.let { putExtra("userName", it) }
            }

            "CLIENT" -> Intent(this, ClientActivity::class.java).apply {
                putExtra("userId", state.userId)
                putExtra("userEmail", state.email)
                state.userName?.let { putExtra("userName", it) }
            }

            else -> Intent(this, ClientActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupClickListeners() {
        binding.signup.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }
    }
}