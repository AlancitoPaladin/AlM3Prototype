package com.itsm.prototype

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.itsm.prototype.ui.client.ClientActivity
import com.itsm.prototype.ui.login.LoginActivity
import com.itsm.prototype.ui.seller.SellerActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkAndNavigate()
    }

    private fun checkAndNavigate() {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        val userType = prefs.getString("userType", "")

        val intent = when {
            !isLoggedIn -> Intent(this, LoginActivity::class.java)
            userType == "SELLER" -> Intent(this, SellerActivity::class.java)
            userType == "CLIENT" -> Intent(this, ClientActivity::class.java)
            else -> Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}