package com.example.smart_city.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_city.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Pindah ke MainActivity setelah 2.5 detik
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref = getSharedPreferences("smartcity_session", MODE_PRIVATE)
            val isLogin = sharedPref.getBoolean("is_login", false)

            if (isLogin) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2500)
    }
}