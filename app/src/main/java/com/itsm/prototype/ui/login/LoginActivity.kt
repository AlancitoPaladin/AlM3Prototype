package com.itsm.prototype.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import com.itsm.prototype.R
import com.itsm.prototype.databinding.ActivityLoginBinding
import com.itsm.prototype.ui.client.ClientActivity
import com.itsm.prototype.ui.seller.SellerActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Success -> {
                    saveLoginSession(state.userType, state.email)
                    showToast("¡Bienvenido!")
                    navigateToAppropriateActivity(state.userType)
                }

                is LoginState.Error -> {
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

    private fun saveLoginSession(userType: String, email: String) {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        prefs.edit {
            putBoolean("isLoggedIn", true)
            putString("userType", userType)
            putString("userEmail", email)
        }
    }

    private fun navigateToAppropriateActivity(userType: String) {
        val intent = when (userType) {
            "SELLER" -> Intent(this, SellerActivity::class.java)
            "CLIENT" -> Intent(this, ClientActivity::class.java)
            else -> Intent(this, ClientActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}