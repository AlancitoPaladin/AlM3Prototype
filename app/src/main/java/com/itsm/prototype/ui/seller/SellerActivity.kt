package com.itsm.prototype.ui.seller

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import com.itsm.prototype.R
import com.itsm.prototype.databinding.ActivitySellerBinding
import com.itsm.prototype.ui.login.LoginActivity

class SellerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySellerBinding
    private val viewModel: SellerViewModel by viewModels()

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
            binding.sellerName = seller.getName()
            binding.storeName = seller.getStoreName()
        }

        viewModel.myModels.observe(this) { models ->

        }

        viewModel.createModelState.observe(this) { state ->
            when (state) {
                is CreateModelState.Success -> {
                    showToast(state.message)
                }

                is CreateModelState.Error -> {
                    showToast(state.message)
                }
            }
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