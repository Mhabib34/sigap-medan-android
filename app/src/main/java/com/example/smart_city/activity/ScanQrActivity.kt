package com.example.smart_city.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.smart_city.R
import com.example.smart_city.helper.DatabaseHelper
import com.example.smart_city.helper.ToastHelper
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class ScanQrActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var barcodeView: DecoratedBarcodeView
    private var misiId: Int = 0
    private var poin: Int = 30
    private var judul: String = ""
    private var userId: Int = 0
    private var sudahScan: Boolean = false

    companion object {
        const val REQ_CAMERA_PERMISSION = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_scan_qr)

        db = DatabaseHelper(this)
        misiId = intent.getIntExtra("misi_id", 0)
        poin = intent.getIntExtra("poin", 30)
        judul = intent.getStringExtra("judul") ?: "Scan QR"
        userId = getSharedPreferences("smartcity_session", MODE_PRIVATE)
            .getInt("user_id", 0)

        // Setup UI
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvJudulScan).text = judul
        findViewById<TextView>(R.id.tvQuestJudul).text = judul
        findViewById<TextView>(R.id.tvQuestPoin).text = "+$poin"
        findViewById<TextView>(R.id.tvInfoScan).text =
            "Scan QR code untuk mendapatkan $poin poin."

        findViewById<TextView>(R.id.tvInputManual).setOnClickListener {
            showManualInputDialog()
        }

        // Setup ZXing scanner
        checkCameraPermissionAndSetup()
    }

    private fun showManualInputDialog() {
        val editText = android.widget.EditText(this).apply {
            hint = "Masukkan kode QR"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setPadding(48, 32, 48, 32)
        }

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Input Kode Manual")
            .setMessage("Masukkan kode yang tertera di lokasi.")
            .setView(editText)
            .setPositiveButton("Konfirmasi") { _, _ ->
                val kode = editText.text.toString().trim()
                if (kode.isEmpty()) {
                    ToastHelper.showError(this, "Kode tidak boleh kosong!")
                } else {
                    prosesQrBerhasil(kode)
                }
            }
            .setNegativeButton("Batal", null)
            .create()

        // Otomatis munculkan keyboard saat dialog terbuka
        dialog.window?.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )
        dialog.show()

        // Request focus ke EditText supaya keyboard langsung muncul
        editText.requestFocus()
    }

    private fun setupScanner() {
        barcodeView = DecoratedBarcodeView(this)
        barcodeView.setStatusText("")
        findViewById<android.widget.FrameLayout>(R.id.scannerContainer)
            .addView(barcodeView)

        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result != null && !sudahScan) {
                    sudahScan = true
                    prosesQrBerhasil(result.text)
                }
            }
        })
    }

    private fun prosesQrBerhasil(qrContent: String) {
        // Untuk demo: scan apapun langsung dapat poin
        val poinDapat = db.selesaikanMisi(userId, misiId)

        runOnUiThread {
            if (poinDapat > 0) {
                ToastHelper.showSuccess(this, "🎉 QR Scan berhasil! +$poinDapat poin")
            } else {
                ToastHelper.showError(this, "Misi sudah diselesaikan hari ini!")
            }
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::barcodeView.isInitialized) barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        if (::barcodeView.isInitialized) barcodeView.pause()
    }

    private fun checkCameraPermissionAndSetup() {
        when {
            // Sudah ada izin → langsung setup scanner
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                setupScanner()
            }

            // Permanently denied → dialog ke Settings lalu finish()
            isCameraPermissionEverRequested() &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Izin Kamera Diblokir")
                    .setMessage("Izin kamera telah ditolak secara permanen. Aktifkan di Pengaturan aplikasi untuk bisa scan QR.")
                    .setPositiveButton("Buka Pengaturan") { _, _ ->
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", packageName, null)
                        }
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("Batal") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()
            }

            // Belum pernah diminta → minta izin pertama kali
            else -> {
                getSharedPreferences("smartcity_perm", MODE_PRIVATE)
                    .edit().putBoolean("camera_requested", true).apply()

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    REQ_CAMERA_PERMISSION
                )
            }
        }
    }

    // ✅ Tambah helper ini
    private fun isCameraPermissionEverRequested(): Boolean {
        return getSharedPreferences("smartcity_perm", MODE_PRIVATE)
            .getBoolean("camera_requested", false)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CAMERA_PERMISSION) {
            when {
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    setupScanner()
                }
                // Deny sekali → finish() (sesuai behavior ScanQr)
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                    ToastHelper.showError(this, "Izin kamera diperlukan untuk scan QR!")
                    finish()
                }
                // Permanently denied → dialog ke Settings
                else -> {
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Izin Kamera Diblokir")
                        .setMessage("Izin kamera telah ditolak secara permanen. Aktifkan di Pengaturan aplikasi untuk bisa scan QR.")
                        .setPositiveButton("Buka Pengaturan") { _, _ ->
                            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", packageName, null)
                            }
                            startActivity(intent)
                            finish()
                        }
                        .setNegativeButton("Batal") { _, _ -> finish() }
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }
}