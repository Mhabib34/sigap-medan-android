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
import androidx.core.view.WindowCompat
import com.example.smart_city.R
import com.example.smart_city.helper.DatabaseHelper
import com.example.smart_city.helper.PasswordHelper
import com.example.smart_city.helper.ToastHelper
import java.security.MessageDigest

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val etPassword = findViewById<EditText>(R.id.etPassword)
        val ivShowPassword = findViewById<ImageView>(R.id.ivShowPassword)
        ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text.length)
        }

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

        // Tombol Daftar
        findViewById<Button>(R.id.btnDaftar).setOnClickListener {
            prosesRegister()
        }

        // Klik "Masuk" pindah ke LoginActivity
        tvMasuk.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun prosesRegister() {
        val nama = findViewById<EditText>(R.id.etNama).text.toString().trim()
        val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()
        val kota = "Medan"

        // Validasi input
        if (nama.isEmpty()) {
            findViewById<EditText>(R.id.etNama).error = "Nama tidak boleh kosong"
            return
        }
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
        if (password.length < 8) {
            findViewById<EditText>(R.id.etPassword).error = "Password minimal 8 karakter"
            return
        }

        // Cek email sudah terdaftar
        if (dbHelper.isEmailExist(email)) {
            findViewById<EditText>(R.id.etEmail).error = "Email sudah terdaftar"
            return
        }

        // Simpan ke database
        val hashedPassword = PasswordHelper.hash(password)
        val berhasil = dbHelper.registerUser(nama, email, hashedPassword, kota)

        if (berhasil) {
            ToastHelper.showSuccess(this, "Registrasi berhasil!")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            ToastHelper.showError(this, "Email sudah terdaftar!")
        }
    }
}