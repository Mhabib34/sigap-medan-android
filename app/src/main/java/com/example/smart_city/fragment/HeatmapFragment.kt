package com.example.smart_city.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.smart_city.R
import com.example.smart_city.helper.DatabaseHelper
import com.example.smart_city.manager.UserXpManager
import com.example.smart_city.model.CityReport
import com.example.smart_city.model.DummyData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.*

class HeatmapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val circleOverlays = mutableListOf<Circle>()
    private val markerList = mutableListOf<Marker>()
    private val markerReportMap = mutableMapOf<String, CityReport>()

    // Radius cluster dalam derajat (~200 meter)
    private val CLUSTER_RADIUS = 0.003

    // Pulse animation state
    private val handler = Handler(Looper.getMainLooper())
    private var pulseExpand = true
    private var isAnimating = false

    private lateinit var db: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_heatmap, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        updateStatsCard(view)

        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fabFilter
        )?.setOnClickListener {
            showLegendDialog()
        }
    }

    private fun updateStatsCard(view: View) {
        val allReports = getAllReports()
        val totalReports = allReports.size
        val dailyGoal = 20

        // ── City Impact Score ──────────────────────────────────────────
        // Rata-rata severity semua laporan, diskala ke 0–100
        val avgSeverity = if (allReports.isEmpty()) 0.0
        else allReports.map { it.severity }.average()
        val impactScore = ((avgSeverity / 10.0) * 100).toInt().coerceIn(0, 100)

        // ── +X% badge ─────────────────────────────────────────────────
        // Bandingkan laporan DB hari ini vs kemarin
        val hariIni  = db.getLaporanHariIni()
        val kemarin  = db.getLaporanKemarin()
        val trendText = when {
            kemarin == 0 && hariIni == 0 -> "─ 0%"
            kemarin == 0                 -> "↑ +100%"
            else -> {
                val pct = ((hariIni - kemarin).toFloat() / kemarin * 100).toInt()
                if (pct >= 0) "↑ +$pct%" else "↓ $pct%"
            }
        }

        // ── Progress bar ───────────────────────────────────────────────
        val progress = ((totalReports.toFloat() / dailyGoal) * 100)
            .coerceIn(0f, 100f).toInt()

        // ── Bind ke view ───────────────────────────────────────────────
        view.findViewById<TextView>(R.id.tvImpactScore)?.text   = impactScore.toString()
        view.findViewById<TextView>(R.id.tvReportCount)?.text   = "$totalReports LAPORAN"
        view.findViewById<ProgressBar>(R.id.progressImpact)?.progress = progress

        // Trend badge — cari TextView yang sekarang hardcode "↑ +5%"
        // Tambahkan id di XML-nya: android:id="@+id/tvTrend"
        view.findViewById<TextView>(R.id.tvTrend)?.apply {
            text = trendText
            setTextColor(
                if (trendText.startsWith("↓"))
                    android.graphics.Color.parseColor("#E8541A")   // merah kalau turun
                else
                    android.graphics.Color.parseColor("#1D9E75")   // hijau kalau naik/netral
            )
        }
    }

    // Helper: konversi row DB laporan → CityReport
    private fun dbRowToCityReport(row: Map<String, String>): CityReport? {
        val lat = row["lat"]?.toDoubleOrNull() ?: return null
        val lng = row["lng"]?.toDoubleOrNull() ?: return null
        // Tentukan severity & type berdasarkan judul (heuristik sederhana)
        val judul = row["judul"] ?: return null
        val (type, severity) = when {
            judul.contains("jalan", ignoreCase = true) ||
                    judul.contains("berlubang", ignoreCase = true) ||
                    judul.contains("aspal", ignoreCase = true)   -> "Kerusakan Jalan" to 7
            judul.contains("sampah", ignoreCase = true)  -> "Sampah" to 5
            judul.contains("banjir", ignoreCase = true)  -> "Banjir" to 8
            judul.contains("macet", ignoreCase = true) ||
                    judul.contains("kemacetan", ignoreCase = true) -> "Kemacetan" to 6
            judul.contains("listrik", ignoreCase = true) ||
                    judul.contains("lampu", ignoreCase = true)   -> "Listrik" to 4
            judul.contains("pohon", ignoreCase = true)   -> "Kedaruratan" to 7
            else                                         -> "Laporan Warga" to 5
        }
        // Hitung waktu relatif dari tanggal
        val tanggal = row["tanggal"] ?: ""
        val lastUpdated = try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val date = sdf.parse(tanggal)
            val diff = System.currentTimeMillis() - (date?.time ?: 0L)
            val menit = diff / 60000
            when {
                menit < 60   -> "$menit menit lalu"
                menit < 1440 -> "${menit / 60} jam lalu"
                else         -> "${menit / 1440} hari lalu"
            }
        } catch (e: Exception) { "Baru saja" }

        return CityReport(
            id           = "db_${row["id"]}",
            title        = judul,
            type         = type,
            lat          = lat,
            lng          = lng,
            impactPoints = row["lat"]?.let { 30 } ?: 30, // default 30 XP untuk laporan user
            severity     = severity,
            imageUrl     = row["foto"] ?: "https://picsum.photos/seed/user${row["id"]}/300/200",
            address      = row["lokasi"] ?: "Medan",
            lastUpdated  = lastUpdated
        )
    }

    // Ambil gabungan semua report (Dummy + DB)
    private fun getAllReports(): List<CityReport> {
        val dbReports = db.getAllLaporan()
            .mapNotNull { dbRowToCityReport(it) }
        // Gabungkan; DB reports ditaruh di depan agar lebih prioritas
        return dbReports + DummyData.reports
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // UI Settings
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = false
            isCompassEnabled = false
        }

        // Aktifkan lokasi jika ada permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001
            )
        }

        // Fokus ke Medan
        val medan = LatLng(3.5952, 98.6722)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(medan, 13f))

        // Gambar heatmap & markers
        drawHeatmap()
        addMarkers()

        // Handle tap marker → tampilkan bottom sheet
        googleMap.setOnMarkerClickListener { marker ->
            markerReportMap[marker.id]?.let { report ->
                showBottomSheet(report)
            }
            true
        }

        // Animasi pulse
        if (!isAnimating) {
            startPulseAnimation()
            isAnimating = true
        }
    }

    // ===== HEATMAP LOGIC =====

    private fun drawHeatmap() {
        circleOverlays.forEach { it.remove() }
        circleOverlays.clear()

        val reports = getAllReports()
        val visited = BooleanArray(reports.size)

        for (i in reports.indices) {
            if (visited[i]) continue

            val cluster = mutableListOf(reports[i])
            for (j in i + 1 until reports.size) {
                if (!visited[j] && isNearby(reports[i], reports[j])) {
                    cluster.add(reports[j])
                    visited[j] = true
                }
            }
            visited[i] = true

            val centerLat = cluster.map { it.lat }.average()
            val centerLng = cluster.map { it.lng }.average()
            val count = cluster.size

            val (fillColor, strokeColor, radius) = when {
                count >= 6 -> Triple(
                    Color.argb(80, 232, 84, 26),
                    Color.argb(180, 232, 84, 26),
                    350.0
                )
                count >= 3 -> Triple(
                    Color.argb(80, 245, 196, 0),
                    Color.argb(180, 245, 196, 0),
                    280.0
                )
                else -> Triple(
                    Color.argb(80, 29, 158, 117),
                    Color.argb(180, 29, 158, 117),
                    200.0
                )
            }

            val circle = googleMap.addCircle(
                CircleOptions()
                    .center(LatLng(centerLat, centerLng))
                    .radius(radius)
                    .fillColor(fillColor)
                    .strokeColor(strokeColor)
                    .strokeWidth(2f)
            )
            circleOverlays.add(circle)
        }
    }

    private fun isNearby(a: CityReport, b: CityReport): Boolean {
        val dlat = a.lat - b.lat
        val dlng = a.lng - b.lng
        return sqrt(dlat * dlat + dlng * dlng) < CLUSTER_RADIUS
    }

    // ===== MARKERS =====

    private fun addMarkers() {
        markerList.forEach { it.remove() }
        markerList.clear()
        markerReportMap.clear()

        getAllReports().forEach { report ->
            val icon = getMarkerIcon(report.severity)
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(report.lat, report.lng))
                    .title(report.title)
                    .icon(icon)
                    .anchor(0.5f, 0.5f)
                    .snippet(report.address)
            )
            marker?.let {
                markerList.add(it)
                markerReportMap[it.id] = report
            }
        }
    }

    private fun getMarkerIcon(severity: Int): BitmapDescriptor {
        val hue = when {
            severity >= 7 -> BitmapDescriptorFactory.HUE_RED
            severity >= 4 -> BitmapDescriptorFactory.HUE_YELLOW
            else -> BitmapDescriptorFactory.HUE_GREEN
        }
        return BitmapDescriptorFactory.defaultMarker(hue)
    }

    // ===== PULSE ANIMATION =====

    private fun startPulseAnimation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isAdded) return
                circleOverlays.forEach { circle ->
                    val currentRadius = circle.radius
                    circle.radius = if (pulseExpand) currentRadius * 1.04 else currentRadius * (1.0 / 1.04)
                }
                pulseExpand = !pulseExpand
                handler.postDelayed(this, 1200)
            }
        }, 1200)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        isAnimating = false
    }

    // ===== BOTTOM SHEET =====

    private fun showBottomSheet(report: CityReport) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_report, null)

        // Isi data (tidak berubah)
        sheetView.findViewById<TextView>(R.id.tvReportTitle).text = report.title
        sheetView.findViewById<TextView>(R.id.tvReportType).text = report.type
        sheetView.findViewById<TextView>(R.id.tvReportAddress).text = report.address
        sheetView.findViewById<TextView>(R.id.tvXpBadge).text = "+${report.impactPoints} XP"
        sheetView.findViewById<TextView>(R.id.tvLastUpdated).text =
            "Terakhir diperbarui: ${report.lastUpdated}"

        Glide.with(this)
            .load(report.imageUrl)
            .placeholder(android.R.color.darker_gray)
            .centerCrop()
            .into(sheetView.findViewById<ImageView>(R.id.ivReportImage))

        // ===== UPVOTE LOGIC =====
        val btnUpvote = sheetView.findViewById<Button>(R.id.btnUpvote)
        val alreadyUpvoted = UserXpManager.hasUpvoted(requireContext(), report.id)

        if (alreadyUpvoted) {
            // Tampilkan state sudah di-upvote
            btnUpvote.text = "✓ UPVOTED"
            btnUpvote.isEnabled = false
            btnUpvote.alpha = 0.5f
        }

        btnUpvote.setOnClickListener {
            if (UserXpManager.hasUpvoted(requireContext(), report.id)) {
                Toast.makeText(requireContext(), "Kamu sudah upvote laporan ini.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan upvote & tambah XP
            UserXpManager.markUpvoted(requireContext(), report.id)
            UserXpManager.addXp(requireContext(), report.impactPoints)

            val totalXp = UserXpManager.getTotalXp(requireContext())
            Toast.makeText(
                requireContext(),
                "✅ Upvote berhasil! +${report.impactPoints} XP  |  Total XP: $totalXp",
                Toast.LENGTH_SHORT
            ).show()

            // Update tampilan tombol
            btnUpvote.text = "✓ UPVOTED"
            btnUpvote.isEnabled = false
            btnUpvote.alpha = 0.5f

            dialog.dismiss()
        }

        // Tombol DETAILS (tidak berubah)
        sheetView.findViewById<Button>(R.id.btnDetails).setOnClickListener {
            Toast.makeText(requireContext(), "📋 Detail: ${report.title}", Toast.LENGTH_SHORT).show()
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }
    // ===== LEGEND DIALOG =====

    private fun showLegendDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_legend, null)

        // Siapkan data
        val allReportsRaw = db.getAllLaporan()
        val allDummyRaw = DummyData.reports.map {
            mapOf("lat" to it.lat.toString(), "lng" to it.lng.toString())
        }
        val combined = allReportsRaw + allDummyRaw

        val (thresholdKritis, thresholdSedang) = db.getThresholds(combined)
        val (jumlahKritis, jumlahSedang, jumlahRendah) = db.getClusterStats(combined)

        // Bind ke TextView di XML
        view.findViewById<TextView>(R.id.tvLabelKritis)?.text =
            "Kritis (≥$thresholdKritis laporan)"
        view.findViewById<TextView>(R.id.tvLabelSedang)?.text =
            "Sedang ($thresholdSedang–${thresholdKritis - 1} laporan)"
        view.findViewById<TextView>(R.id.tvLabelRendah)?.text =
            "Rendah (1–${thresholdSedang - 1} laporan)"

        view.findViewById<TextView>(R.id.tvSubKritis)?.text =
            "$jumlahKritis klaster aktif"
        view.findViewById<TextView>(R.id.tvSubSedang)?.text =
            "$jumlahSedang klaster aktif"
        view.findViewById<TextView>(R.id.tvSubRendah)?.text =
            "$jumlahRendah klaster aktif"

        dialog.setContentView(view)
        dialog.show()
    }

    // ===== PERMISSION RESULT =====

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == 1001 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap.isMyLocationEnabled = true
            }
        }
    }
}