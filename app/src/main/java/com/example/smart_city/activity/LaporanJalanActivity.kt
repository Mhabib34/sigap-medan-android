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

        findViewById<Button>(R.id.btnGaleri).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQ_GALERI)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardFoto).setOnClickListener {
            showFotoOptions()
        }

        // ✅ Ganti Lokasi → buka LocationPickerActivity
        findViewById<Button>(R.id.btnGantiLokasi).setOnClickListener {
            val intent = Intent(this, LocationPickerActivity::class.java).apply {
                putExtra("current_lat", latitude)
                putExtra("current_lng", longitude)
                putExtra("current_alamat", alamat)
            }
            startActivityForResult(intent, REQ_LOCATION_PICKER)
        }

        findViewById<Button>(R.id.btnKirimLaporan).setOnClickListener {
            kirimLaporan()
        }

    }

    // Hapus getLocation() dari onCreate, ganti dengan ini:
    override fun onResume() {
        super.onResume()
        // Jangan ambil GPS kalau user sudah pilih manual
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQ_PERMISSION
            )
        } else {
            openKamera()
        }
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
        // ✅ Guard di luar callback, bukan hanya di dalam
        if (isLocationManuallySet) return

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                103
            )
            return
        }

        fusedLocation.lastLocation.addOnSuccessListener { location ->
            // ✅ Double-check di dalam callback juga (untuk race condition)
            if (isLocationManuallySet) return@addOnSuccessListener

            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                try {
                    val geocoder = Geocoder(this, Locale("id", "ID"))
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        alamat = addr.thoroughfare ?: addr.subLocality ?: "Medan"
                        val kota = addr.subAdminArea ?: "Kota Medan"
                        runOnUiThread {
                            findViewById<TextView>(R.id.tvAlamat).text = alamat
                            findViewById<TextView>(R.id.tvKota).text = "$kota, Sumut"
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        findViewById<TextView>(R.id.tvAlamat).text = "Jl. Medan"
                    }
                }
            } else {
                latitude  = 3.5952
                longitude = 98.6722
                alamat    = "Jl. Sudirman"
                runOnUiThread {
                    findViewById<TextView>(R.id.tvAlamat).text = alamat
                    findViewById<TextView>(R.id.tvKota).text   = "Kota Medan, Sumut"
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

            // ✅ Terima hasil dari LocationPickerActivity
            REQ_LOCATION_PICKER -> {
                // ✅ Set flag PERTAMA sebelum assign nilai apapun
                isLocationManuallySet = true

                val newLat    = data?.getDoubleExtra("lat", latitude)    ?: latitude
                val newLng    = data?.getDoubleExtra("lng", longitude)   ?: longitude
                val newAlamat = data?.getStringExtra("alamat")           ?: alamat

                latitude  = newLat
                longitude = newLng
                alamat    = newAlamat

                runOnUiThread {
                    findViewById<TextView>(R.id.tvAlamat).text = newAlamat
                    // Update tvKota juga supaya konsisten
                    findViewById<TextView>(R.id.tvKota).text = "Lokasi dipilih manual"
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
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) openKamera()
                else ToastHelper.showError(this, "Izin kamera diperlukan!")
            }
            103 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) getLocation()
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
            userId, judul, catatan, fotoPath, alamat, latitude, longitude
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