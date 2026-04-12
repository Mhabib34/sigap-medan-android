package com.example.smart_city

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Warnai kata "Masuk" jadi oranye
        val tvMasuk = findViewById<TextView>(R.id.tvMasuk)
        val text = "Sudah punya akun? Masuk"
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.orange)),
            text.indexOf("Masuk"),
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvMasuk.text = spannable

        // Tombol daftar
        findViewById<android.widget.Button>(R.id.btnDaftar).setOnClickListener {
            // TODO: proses register
        }

        // Klik "Masuk" pindah ke LoginActivity
        tvMasuk.setOnClickListener {
            // startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}