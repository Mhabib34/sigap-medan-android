package com.example.smart_city.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smart_city.MainActivity
import com.example.smart_city.R
import com.example.smart_city.helper.DatabaseHelper
import com.example.smart_city.helper.PasswordHelper
import com.example.smart_city.helper.ToastHelper

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        val etPassword = findViewById<EditText>(R.id.etPassword)
        val ivShowPassword = findViewById<ImageView>(R.id.ivShowPassword)

        // Toggle show/hide password
        ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // Warnai kata "Daftar" jadi oranye
        findViewById<TextView>(R.id.tvDaftar).apply {
            val text = "Belum punya akun? Daftar"
            val spannable = SpannableString(text)
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)),
                text.indexOf("Daftar"),
                text.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setText(spannable)

            setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }

        // Tombol Masuk
        findViewById<Button>(R.id.btnMasuk).setOnClickListener {
            prosesLogin()
        }
    }

    private fun prosesLogin() {
        val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()

        // Validasi input kosong
        if (email.isEmpty()) {
            findViewById<EditText>(R.id.etEmail).error = "Email tidak boleh kosong"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            findViewById<EditText>(R.id.etEmail).error = "Format email tidak valid"
            return
        }
        if (password.isEmpty()) {
            findViewById<EditText>(R.id.etPassword).error = "Password tidak boleh kosong"
            return
        }

        // Cek login ke database
        val hashedPassword = PasswordHelper.hash(password)
        val berhasil = dbHelper.loginUser(email, hashedPassword)

        if (berhasil) {
            // Simpan session user
            val user = dbHelper.getUserByEmail(email)
            val sharedPref = getSharedPreferences("smartcity_session", MODE_PRIVATE)
            sharedPref.edit().apply {
                putBoolean("is_login", true)
                putInt("user_id", user?.get("id")?.toInt() ?: 0) // tambah ini
                putString("nama", user?.get("nama"))
                putString("email", user?.get("email"))
                putString("kota", user?.get("kota"))
                apply()
            }

            ToastHelper.showSuccess(this, "Selamat datang, ${user?.get("nama")}!")

            // Pindah ke MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            ToastHelper.showError(this, "Email atau password salah!")
        }
    }

}