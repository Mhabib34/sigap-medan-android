package com.example.smart_city.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.smart_city.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var selectedLat: Double = 3.5952
    private var selectedLng: Double = 98.6722

    private lateinit var tvKoordinat: TextView
    private var selectedAlamat: String = ""
    private lateinit var tvAlamatPicker: TextView
    private lateinit var btnKonfirmasi: Button
    private lateinit var pbAlamat: ProgressBar

    private val geocodeHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val geocodeRunnable = Runnable { doGeocode() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_location_picker)

        tvAlamatPicker = findViewById(R.id.tvAlamatPicker)
        btnKonfirmasi  = findViewById(R.id.btnKonfirmasiLokasi)
        pbAlamat       = findViewById(R.id.pbAlamat)
        tvKoordinat = findViewById(R.id.tvKoordinat)

        selectedLat = intent.getDoubleExtra("current_lat", 3.5952)
        selectedLng = intent.getDoubleExtra("current_lng", 98.6722)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapPickerFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<android.widget.ImageView>(R.id.btnBackPicker).setOnClickListener {
            finish()
        }

        btnKonfirmasi.setOnClickListener {
            if (selectedAlamat.isEmpty()) {
                Toast.makeText(this, "Tunggu sebentar, sedang mendapatkan alamat...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val result = Intent().apply {
                putExtra("lat", selectedLat)
                putExtra("lng", selectedLng)
                putExtra("alamat", selectedAlamat)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        geocodeHandler.removeCallbacks(geocodeRunnable)
    }

    private fun doGeocode() {
        val lat = selectedLat
        val lng = selectedLng

        Thread {
            try {
                val geocoder = Geocoder(this, Locale("id", "ID"))
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                val hasil = if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    buildString {
                        addr.thoroughfare?.let { append(it) }
                        addr.featureName?.let {
                            if (it != addr.thoroughfare) {
                                if (isNotEmpty()) append(" No.")
                                append(it)
                            }
                        }
                        addr.subLocality?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                        addr.locality?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                        addr.subAdminArea?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                        addr.adminArea?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                        if (isEmpty()) append("Lat: %.4f, Lng: %.4f".format(lat, lng))
                    }
                } else {
                    "Lat: %.4f, Lng: %.4f".format(lat, lng)
                }

                runOnUiThread {
                    selectedAlamat = hasil
                    tvAlamatPicker.text = hasil
                    pbAlamat.visibility = View.GONE
                    btnKonfirmasi.isEnabled = true
                }
            } catch (e: Exception) {
                val fallback = "Lat: %.4f, Lng: %.4f".format(lat, lng)
                runOnUiThread {
                    selectedAlamat = fallback
                    tvAlamatPicker.text = fallback
                    pbAlamat.visibility = View.GONE
                    btnKonfirmasi.isEnabled = true
                }
            }
        }.start()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = false

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }

        // Mulai di lokasi sekarang
        val awal = LatLng(selectedLat, selectedLng)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(awal, 15f))

        // Pasang alamat awal jika ada
        val alamatAwal = intent.getStringExtra("current_alamat")
        if (!alamatAwal.isNullOrEmpty()) {
            selectedAlamat = alamatAwal
            tvAlamatPicker.text = alamatAwal
            btnKonfirmasi.isEnabled = true
        }

        // ✅ Saat peta mulai digeser — disable tombol & tampilkan loading
        googleMap.setOnCameraMoveStartedListener {
            btnKonfirmasi.isEnabled = false
            selectedAlamat = ""
            pbAlamat.visibility = View.VISIBLE
            tvAlamatPicker.text = "Mendapatkan alamat..."
            tvKoordinat.text = "" // ✅ Kosongkan saat geser
        }

        // ✅ Saat peta berhenti digeser — ambil koordinat tengah & geocoding
        googleMap.setOnCameraIdleListener {
            val center = googleMap.cameraPosition.target
            selectedLat = center.latitude
            selectedLng = center.longitude

            tvKoordinat.text = "🌐 %.6f, %.6f".format(center.latitude, center.longitude)

            geocodeHandler.removeCallbacks(geocodeRunnable)
            geocodeHandler.postDelayed(geocodeRunnable, 1500)
        }
    }
}