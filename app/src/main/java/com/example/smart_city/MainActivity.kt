package com.example.smart_city

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        // Tahan splash selama isLoading = true
        splashScreen.setKeepOnScreenCondition { isLoading }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Simulasi loading (misal fetch data dari server)
        lifecycleScope.launch {
            delay(2000) // 2 detik
            isLoading = false // Splash akan hilang
        }
    }
}