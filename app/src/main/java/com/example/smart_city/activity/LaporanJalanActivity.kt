package com.example.smart_city.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.smart_city.R
import com.example.smart_city.helper.DatabaseHelper
import com.example.smart_city.helper.ToastHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LaporanJalanActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var fusedLocation: FusedLocationProviderClient
    private var fotoUri: Uri? = null
    private var fotoPath: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var alamat: String = "Mendapatkan lokasi..."
    private var misiId: Int = 0
    private var poin: Int = 50
    private var userId: Int = 0

    companion object {
        const val REQ_KAMERA = 100
        const val REQ_GALERI = 101
        const val REQ_PERMISSION = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_laporan_jalan)

        db = DatabaseHelper(this)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        misiId = intent.getIntExtra("misi_id", 0)
        poin = intent.getIntExtra("poin", 50)
        userId = getSharedPreferences("smartcity_session", MODE_PRIVATE)
            .getInt("user_id", 0)

        // Tombol back
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Tombol kamera
        findViewById<Button>(R.id.btnKamera).setOnClickListener {
            requestPermissionsAndOpenCamera()
        }

        // Tombol galeri
        findViewById<Button>(R.id.btnGaleri).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQ_GALERI)
        }

        // Klik area foto juga buka pilihan
        findViewById<androidx.cardview.widget.CardView>(R.id.cardFoto).setOnClickListener {
            showFotoOptions()
        }

        // Tombol ganti lokasi
        findViewById<Button>(R.id.btnGantiLokasi).setOnClickListener {
            getLocation()
        }

        // Tombol kirim
        findViewById<Button>(R.id.btnKirimLaporan).setOnClickListener {
            kirimLaporan()
        }

        // Ambil lokasi otomatis
        getLocation()
    }

    private fun showFotoOptions() {
        val options = arrayOf("📷 Ambil Foto", "🖼️ Pilih dari Galeri")
        android.app.AlertDialog.Builder(this)
            .setTitle("Pilih Foto")
            .setItems(options) { _, which ->
                if (which == 0) requestPermissionsAndOpenCamera()
                else {
                    val intent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, REQ_GALERI)
                }
            }.show()
    }

    private fun requestPermissionsAndOpenCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), REQ_PERMISSION)
        } else {
            openKamera()
        }
    }

    private fun openKamera() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fotoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "LAPORAN_$timeStamp.jpg")
        fotoPath = fotoFile.absolutePath
        fotoUri = FileProvider.getUriForFile(this,
            "${packageName}.provider", fotoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
        startActivityForResult(intent, REQ_KAMERA)
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), 103)
            return
        }

        fusedLocation.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude

                // Reverse geocoding
                try {
                    val geocoder = Geocoder(this, Locale("id", "ID"))
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        alamat = addr.thoroughfare ?: addr.subLocality ?: "Medan"
                        val kota = addr.subAdminArea ?: "Kota Medan"
                        findViewById<TextView>(R.id.tvAlamat).text = alamat
                        findViewById<TextView>(R.id.tvKota).text = "$kota, Sumut"
                    }
                } catch (e: Exception) {
                    findViewById<TextView>(R.id.tvAlamat).text = "Jl. Medan"
                }
            } else {
                // Fallback hardcoded
                latitude = 3.5952
                longitude = 98.6722
                alamat = "Jl. Sudirman"
                findViewById<TextView>(R.id.tvAlamat).text = alamat
                findViewById<TextView>(R.id.tvKota).text = "Kota Medan, Sumut"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_KAMERA -> {
                    // Foto dari kamera
                    fotoUri?.let {
                        val ivPreview = findViewById<ImageView>(R.id.ivFotoPreview)
                        val layoutUpload = findViewById<android.view.View>(R.id.layoutUpload)
                        ivPreview.setImageURI(it)
                        ivPreview.visibility = android.view.View.VISIBLE
                        layoutUpload.visibility = android.view.View.GONE
                    }
                }
                REQ_GALERI -> {
                    // Foto dari galeri
                    data?.data?.let { uri ->
                        fotoUri = uri
                        fotoPath = uri.toString()
                        val ivPreview = findViewById<ImageView>(R.id.ivFotoPreview)
                        val layoutUpload = findViewById<android.view.View>(R.id.layoutUpload)
                        ivPreview.setImageURI(uri)
                        ivPreview.visibility = android.view.View.VISIBLE
                        layoutUpload.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_PERMISSION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openKamera()
                } else {
                    ToastHelper.showError(this, "Izin kamera diperlukan!")
                }
            }
            103 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                }
            }
        }
    }

    private fun kirimLaporan() {
        if (fotoPath.isEmpty()) {
            ToastHelper.showError(this, "Foto wajib diambil!")
            return
        }

        val catatan = findViewById<EditText>(R.id.etCatatan).text.toString().trim()

        // Simpan laporan ke DB
        val berhasil = db.simpanLaporan(
            userId, "Laporkan Jalan Berlubang",
            catatan, fotoPath, alamat, latitude, longitude
        )

        if (berhasil) {
            // Selesaikan misi + dapat poin
            val poinDapat = db.selesaikanMisi(userId, misiId)
            if (poinDapat > 0) {
                ToastHelper.showSuccess(this, "🎉 Laporan terkirim! +$poinDapat poin")
            } else {
                ToastHelper.showSuccess(this, "Laporan berhasil dikirim!")
            }
            finish()
        } else {
            ToastHelper.showError(this, "Gagal mengirim laporan!")
        }
    }
}