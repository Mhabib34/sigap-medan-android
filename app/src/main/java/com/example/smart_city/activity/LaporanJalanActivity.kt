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

    private var isLocationManuallySet = false
    private var fotoUri: Uri? = null
    private var fotoPath: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var alamat: String = "Mendapatkan lokasi..."
    private var misiId: Int = 0
    private var poin: Int = 50
    private var userId: Int = 0

    private var judul: String = ""
    private var lokasi: String = ""
    private var kategori: String = ""

    companion object {
        const val REQ_KAMERA = 100
        const val REQ_GALERI = 101
        const val REQ_PERMISSION = 102
        const val REQ_LOCATION_PICKER = 104
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_laporan_jalan)

        db = DatabaseHelper(this)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        misiId   = intent.getIntExtra("misi_id", 0)
        poin     = intent.getIntExtra("poin", 50)
        judul    = intent.getStringExtra("judul")    ?: "Laporkan Jalan Berlubang"
        lokasi   = intent.getStringExtra("lokasi")   ?: "Wilayah Medan"
        kategori = intent.getStringExtra("kategori") ?: "laporan"
        userId   = getSharedPreferences("smartcity_session", MODE_PRIVATE)
            .getInt("user_id", 0)

        findViewById<TextView>(R.id.tvJudulLaporan).text = judul
        findViewById<TextView>(R.id.tvFotoLabel).text = "Ambil Foto $judul"
        findViewById<TextView>(R.id.tvFotoSubLabel).text = when {
            judul.contains("Jalan Berlubang", ignoreCase = true)  -> "Pastikan lubang jalan terlihat jelas"
            judul.contains("Kemacetan", ignoreCase = true)        -> "Pastikan kondisi kemacetan terlihat jelas"
            judul.contains("Drainase", ignoreCase = true)         -> "Pastikan saluran drainase yang tersumbat terlihat jelas"
            judul.contains("TPS", ignoreCase = true)              -> "Pastikan kondisi TPS yang penuh terlihat jelas"
            judul.contains("Parkir Liar", ignoreCase = true)      -> "Pastikan kendaraan yang parkir liar terlihat jelas"
            judul.contains("Lampu Jalan", ignoreCase = true)      -> "Pastikan tiang/lampu jalan yang mati terlihat jelas"
            else                                                   -> "Pastikan objek terlihat jelas"
        }

        val etCatatan = findViewById<EditText>(R.id.etCatatan)
        etCatatan.hint = when {
            judul.contains("Jalan Berlubang", ignoreCase = true)  ->
                "Berikan detail kondisi jalan (kedalaman, luas, atau kendala lainnya)"
            judul.contains("Kemacetan", ignoreCase = true)        ->
                "Berikan detail kemacetan (panjang antrian, penyebab, atau perkiraan waktu)"
            judul.contains("Drainase", ignoreCase = true)         ->
                "Berikan detail drainase (lokasi tepat, penyebab sumbatan, sudah berapa lama)"
            judul.contains("TPS", ignoreCase = true)              ->
                "Berikan detail kondisi TPS (seberapa penuh, jenis sampah, bau atau tidak)"
            judul.contains("Parkir Liar", ignoreCase = true)      ->
                "Berikan detail parkir liar (jenis kendaraan, mengganggu arus lalu lintas atau tidak)"
            judul.contains("Lampu Jalan", ignoreCase = true)      ->
                "Berikan detail lampu jalan (jumlah yang mati, sudah berapa lama, dampak ke warga)"
            else                                                   ->
                "Tulis catatan tambahan di sini"
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnKamera).setOnClickListener {
            requestPermissionsAndOpenCamera()
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardFoto).setOnClickListener {
            showFotoOptions()
        }

        findViewById<Button>(R.id.btnKirimLaporan).setOnClickListener {
            kirimLaporan()
        }

        // Tombol refresh lokasi
        findViewById<Button>(R.id.btnRefreshLokasi).setOnClickListener {
            isLocationManuallySet = false
            findViewById<TextView>(R.id.tvGpsStatus).text = "🔄 Mendeteksi..."
            getLocation()
            ToastHelper.showSuccess(this, "Lokasi berhasil diperbarui!")
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isLocationManuallySet) {
            getLocation()
        }
    }

    private fun showFotoOptions() {
        val options = arrayOf("📷 Ambil Foto", "🖼️ Pilih dari Galeri")
        android.app.AlertDialog.Builder(this)
            .setTitle("Pilih Foto")
            .setItems(options) { _, which ->
                if (which == 0) requestPermissionsAndOpenCamera()
                else {
                    val intent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(intent, REQ_GALERI)
                }
            }.show()
    }

    private fun requestPermissionsAndOpenCamera() {
        when {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                openKamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Izin Kamera Diperlukan")
                    .setMessage("Aplikasi membutuhkan akses kamera untuk mengambil foto laporan.")
                    .setPositiveButton("Izinkan") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this, arrayOf(Manifest.permission.CAMERA), REQ_PERMISSION
                        )
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
            isCameraPermissionEverRequested() -> {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Izin Kamera Diblokir")
                    .setMessage("Izin kamera telah ditolak secara permanen. Aktifkan di Pengaturan.")
                    .setPositiveButton("Buka Pengaturan") { _, _ ->
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", packageName, null)
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
            else -> {
                getSharedPreferences("smartcity_perm", MODE_PRIVATE)
                    .edit().putBoolean("camera_requested", true).apply()
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), REQ_PERMISSION
                )
            }
        }
    }

    private fun isCameraPermissionEverRequested(): Boolean {
        return getSharedPreferences("smartcity_perm", MODE_PRIVATE)
            .getBoolean("camera_requested", false)
    }

    private fun openKamera() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fotoFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "LAPORAN_$timeStamp.jpg"
        )
        fotoPath = fotoFile.absolutePath
        fotoUri = FileProvider.getUriForFile(this, "${packageName}.provider", fotoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
        startActivityForResult(intent, REQ_KAMERA)
    }

    private fun getLocation() {
        if (isLocationManuallySet) return

        when {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> {
                ambilLokasiGps()
            }
            !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    && isPermissionEverRequested() -> {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Izin Lokasi Diblokir")
                    .setMessage("Izin lokasi telah ditolak secara permanen. Aktifkan di Pengaturan aplikasi.")
                    .setPositiveButton("Buka Pengaturan") { _, _ ->
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", packageName, null)
                        }
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("Batal") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()
            }
            else -> {
                getSharedPreferences("smartcity_perm", MODE_PRIVATE)
                    .edit().putBoolean("location_requested", true).apply()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    103
                )
            }
        }
    }

    private fun isPermissionEverRequested(): Boolean {
        return getSharedPreferences("smartcity_perm", MODE_PRIVATE)
            .getBoolean("location_requested", false)
    }

    private fun ambilLokasiGps() {
        if (isLocationManuallySet) return

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocation.lastLocation.addOnSuccessListener { location ->
            if (isLocationManuallySet) return@addOnSuccessListener

            if (location != null) {
                latitude  = location.latitude
                longitude = location.longitude
                val akurasi = location.accuracy.toInt()

                // Tampilkan koordinat dulu sebelum geocoder selesai
                runOnUiThread {
                    findViewById<TextView>(R.id.tvLatitude).text  = "%.6f° N".format(latitude)
                    findViewById<TextView>(R.id.tvLongitude).text = "%.6f° E".format(longitude)
                    findViewById<TextView>(R.id.tvAkurasi).text   = "±${akurasi}m"
                    findViewById<TextView>(R.id.tvGpsStatus).text = "🔄 Membaca alamat..."
                }

                try {
                    val geocoder  = Geocoder(this, Locale("id", "ID"))
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]

                        // Nama jalan — thoroughfare paling akurat
                        alamat = addr.thoroughfare
                            ?: addr.featureName
                                    ?: addr.subLocality
                                    ?: "Tidak diketahui"

                        // Kelurahan — subLocality
                        val kelurahan = addr.subLocality
                            ?: addr.featureName
                            ?: "Tidak diketahui"

                        // Kecamatan — locality (BUKAN subAdminArea!)
                        // Di Indonesia: locality = kecamatan, subAdminArea = kota/kabupaten
                        val kecamatan = addr.locality
                            ?: addr.subAdminArea
                            ?: "Tidak diketahui"

                        // Kota/Kabupaten — subAdminArea
                        val kotaKabupaten = addr.subAdminArea
                            ?: addr.adminArea
                            ?: "Tidak diketahui"

                        // Provinsi — adminArea
                        val provinsi = addr.adminArea ?: "Sumut"

                        // Kode Pos — postalCode
                        val kodePos = addr.postalCode ?: "Tidak diketahui"

                        runOnUiThread {
                            // Nama jalan + header kota di baris atas kartu
                            findViewById<TextView>(R.id.tvNamaJalan).text = alamat
                            findViewById<TextView>(R.id.tvKota).text      = "$kotaKabupaten, $provinsi"

                            // Chip detail
                            findViewById<TextView>(R.id.tvKelurahan).text  = kelurahan
                            findViewById<TextView>(R.id.tvKecamatan).text  = kecamatan
                            findViewById<TextView>(R.id.tvKotaChip).text   = kotaKabupaten
                            findViewById<TextView>(R.id.tvKodePos).text    = kodePos

                            findViewById<TextView>(R.id.tvGpsStatus).text = "✅ Lokasi didapat"
                        }
                    } else {
                        // Geocoder berhasil tapi tidak ada hasil
                        runOnUiThread {
                            findViewById<TextView>(R.id.tvNamaJalan).text  = "Alamat tidak ditemukan"
                            findViewById<TextView>(R.id.tvKelurahan).text  = "-"
                            findViewById<TextView>(R.id.tvKecamatan).text  = "-"
                            findViewById<TextView>(R.id.tvKotaChip).text   = "-"
                            findViewById<TextView>(R.id.tvKodePos).text    = "-"
                            findViewById<TextView>(R.id.tvGpsStatus).text  = "⚠️ Alamat kosong"
                        }
                    }
                } catch (e: Exception) {
                    // Geocoder error (misal tidak ada koneksi internet)
                    runOnUiThread {
                        findViewById<TextView>(R.id.tvNamaJalan).text  = "Gagal membaca alamat"
                        findViewById<TextView>(R.id.tvKelurahan).text  = "-"
                        findViewById<TextView>(R.id.tvKecamatan).text  = "-"
                        findViewById<TextView>(R.id.tvKotaChip).text   = "-"
                        findViewById<TextView>(R.id.tvKodePos).text    = "-"
                        findViewById<TextView>(R.id.tvGpsStatus).text  = "⚠️ Error: ${e.message}"
                    }
                }

            } else {
                // lastLocation null — GPS belum siap, gunakan fallback Medan
                latitude  = 3.5952
                longitude = 98.6722
                alamat    = "Jl. Sudirman"
                runOnUiThread {
                    findViewById<TextView>(R.id.tvNamaJalan).text  = alamat
                    findViewById<TextView>(R.id.tvKota).text       = "Kota Medan, Sumut"
                    findViewById<TextView>(R.id.tvKelurahan).text  = "Mendeteksi..."
                    findViewById<TextView>(R.id.tvKecamatan).text  = "Mendeteksi..."
                    findViewById<TextView>(R.id.tvKotaChip).text   = "Kota Medan"
                    findViewById<TextView>(R.id.tvKodePos).text    = "Mendeteksi..."
                    findViewById<TextView>(R.id.tvLatitude).text   = "3.595200° N"
                    findViewById<TextView>(R.id.tvLongitude).text  = "98.672200° E"
                    findViewById<TextView>(R.id.tvAkurasi).text    = "N/A"
                    findViewById<TextView>(R.id.tvGpsStatus).text  = "⚠️ GPS belum siap, coba refresh"
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQ_KAMERA -> {
                fotoUri?.let {
                    val ivPreview    = findViewById<ImageView>(R.id.ivFotoPreview)
                    val layoutUpload = findViewById<android.view.View>(R.id.layoutUpload)
                    ivPreview.setImageURI(it)
                    ivPreview.visibility    = android.view.View.VISIBLE
                    layoutUpload.visibility = android.view.View.GONE
                }
            }
            REQ_GALERI -> {
                data?.data?.let { uri ->
                    fotoUri  = uri
                    fotoPath = uri.toString()
                    val ivPreview    = findViewById<ImageView>(R.id.ivFotoPreview)
                    val layoutUpload = findViewById<android.view.View>(R.id.layoutUpload)
                    ivPreview.setImageURI(uri)
                    ivPreview.visibility    = android.view.View.VISIBLE
                    layoutUpload.visibility = android.view.View.GONE
                }
            }
            REQ_LOCATION_PICKER -> {
                isLocationManuallySet = true

                val newLat    = data?.getDoubleExtra("lat", latitude)  ?: latitude
                val newLng    = data?.getDoubleExtra("lng", longitude) ?: longitude
                val newAlamat = data?.getStringExtra("alamat")         ?: alamat

                latitude  = newLat
                longitude = newLng
                alamat    = newAlamat

                runOnUiThread {
                    findViewById<TextView>(R.id.tvNamaJalan).text  = newAlamat
                    findViewById<TextView>(R.id.tvKota).text       = "Lokasi dipilih manual"
                    findViewById<TextView>(R.id.tvKotaChip).text   = "Manual"
                    findViewById<TextView>(R.id.tvLatitude).text   = "%.6f° N".format(newLat)
                    findViewById<TextView>(R.id.tvLongitude).text  = "%.6f° E".format(newLng)
                    findViewById<TextView>(R.id.tvGpsStatus).text  = "📌 Manual"
                }
                Toast.makeText(this, "📍 Lokasi diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_PERMISSION -> {
                when {
                    grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                        openKamera()
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                        ToastHelper.showError(this, "Izin kamera diperlukan untuk mengambil foto!")
                    }
                    else -> {
                        android.app.AlertDialog.Builder(this)
                            .setTitle("Izin Kamera Diblokir")
                            .setMessage("Izin kamera telah ditolak secara permanen. Aktifkan di Pengaturan.")
                            .setPositiveButton("Buka Pengaturan") { _, _ ->
                                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                                startActivity(intent)
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                }
            }
            103 -> {
                when {
                    grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                        ambilLokasiGps()
                    }
                    else -> {
                        ToastHelper.showError(this, "Izin lokasi diperlukan!")
                        finish()
                    }
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

        val berhasil = db.simpanLaporan(
            userId, judul, catatan, fotoPath, alamat, latitude, longitude,
            kategori  // ← kirim kategori yang sudah diterima dari intent
        )
        if (berhasil) {
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