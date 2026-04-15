package com.example.smart_city.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.smart_city.R
import com.example.smart_city.activity.LaporanJalanActivity
import com.example.smart_city.activity.ScanQrActivity
import com.example.smart_city.helper.DatabaseHelper

class MisiFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private var userId = 0
    private var filterAktif = "semua"
    private val handler = android.os.Handler(android.os.Looper.getMainLooper()) // ← tambah
    private var pendingRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_misi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        val sharedPref = requireActivity()
            .getSharedPreferences("smartcity_session", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", 0)

        loadData(view)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshMisi)
        // Tampilkan skeleton dulu
        swipeRefresh.setOnRefreshListener {
            //load data di beranda
            loadData(view)
        }
    }

    private fun loadData(view: View) {
        val skeleton = view.findViewById<View>(R.id.skeletonMisi)
        val content = view.findViewById<View>(R.id.contentMisi)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshMisi)

            skeleton.visibility = View.VISIBLE
            content.visibility = View.GONE


        pendingRunnable?.let { handler.removeCallbacks(it) }
        pendingRunnable = Runnable {
            if (!isAdded || context == null) return@Runnable

            skeleton.visibility = View.GONE
            content.visibility = View.VISIBLE
            swipeRefresh.isRefreshing = false

            setupFilter(view)
            loadCapaian(view)
            loadMisi(view, filterAktif)
        }
        handler.postDelayed(pendingRunnable!!, 400)
    }

    override fun onResume() {
        super.onResume()
        if (!isAdded || context == null) return // ← tambah
        view?.let {
            if (it.findViewById<View>(R.id.contentMisi).visibility == View.VISIBLE) {
                loadCapaian(it)
                loadMisi(it, filterAktif)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pendingRunnable?.let { handler.removeCallbacks(it) } // ← cancel handler
    }

    private fun setupFilter(view: View) {
        val tabSemua = view.findViewById<TextView>(R.id.tabSemua)
        val tabLingkungan = view.findViewById<TextView>(R.id.tabLingkungan)
        val tabTransportasi = view.findViewById<TextView>(R.id.tabTransportasi)
        val tabLaporan = view.findViewById<TextView>(R.id.tabLaporan)

        tabSemua.setOnClickListener {
            filterAktif = "semua"
            setActiveTab(view, tabSemua, listOf(tabLingkungan, tabTransportasi,tabLaporan))
            loadMisi(view, filterAktif)
        }
        tabLingkungan.setOnClickListener {
            filterAktif = "lingkungan"
            setActiveTab(view, tabLingkungan, listOf(tabSemua, tabTransportasi,tabLaporan))
            loadMisi(view, filterAktif)
        }
        tabTransportasi.setOnClickListener {
            filterAktif = "transportasi"
            setActiveTab(view, tabTransportasi, listOf(tabSemua, tabLingkungan,tabLaporan))
            loadMisi(view, filterAktif)
        }
        tabLaporan.setOnClickListener {
            filterAktif = "laporan"
            setActiveTab(view, tabLaporan, listOf(tabSemua,tabLingkungan,tabTransportasi))
            loadMisi(view, filterAktif)
        }
    }

    private fun setActiveTab(view: View, active: TextView, inactive: List<TextView>) {
        active.setBackgroundResource(R.drawable.bg_tab_misi_active)
        active.setTextColor(Color.WHITE)
        inactive.forEach {
            it.setBackgroundResource(R.drawable.bg_tab_misi_inactive)
            it.setTextColor(Color.parseColor("#888888"))
        }
    }

    private fun loadCapaian(view: View) {
        if (!isAdded || context == null) return
        val user = db.getUserById(userId) ?: return
        val poin = user["poin"]?.toInt() ?: 0
        val target = DatabaseHelper.getTargetPoin(poin)
        val sisaPoin = target - poin
        val progress = ((poin.toFloat() / target) * 100).toInt()

        view.findViewById<TextView>(R.id.tvCapaian).text =
            "Target: $sisaPoin Poin\nLagi Untuk Level Up"
        view.findViewById<ProgressBar>(R.id.progressCapaian).progress = progress
    }

    private fun loadMisi(view: View, filter: String) {
        if (!isAdded || context == null) return
        val container = view.findViewById<LinearLayout>(R.id.containerMisiFragment)
        container.removeAllViews()

        val semuaMisi = db.getMisiHariIni(filter)

        semuaMisi.forEach { misi ->
            val misiId = misi["id"]?.toInt() ?: return@forEach
            val sudahSelesai = db.isMisiSelesai(userId, misiId)
            val kategori = misi["kategori"] ?: ""
            val warna = misi["warna"] ?: "#E8541A"

            val ctx = requireContext() // aman karena sudah cek isAdded di atas
            val itemView = LayoutInflater.from(ctx)
                .inflate(R.layout.item_misi_fragment, container, false)

            // Set data
            itemView.findViewById<View>(R.id.viewStrip)
                .setBackgroundColor(Color.parseColor(warna))
            itemView.findViewById<TextView>(R.id.tvKategori).text = kategori.uppercase()
            itemView.findViewById<TextView>(R.id.tvJudulMisi).text = misi["judul"]
            itemView.findViewById<TextView>(R.id.tvPoinMisi).text = "🪙 ${misi["poin"]} poin"

            // Deskripsi per kategori
            itemView.findViewById<TextView>(R.id.tvDeskripsi).text = when (kategori) {
                "laporan" -> "Ambil foto dan lokasi jalan rusak di sekitar wilayah Medan Baru."
                "transportasi" -> "Naik bus Trans Metro Medan hari ini untuk perjalanan ramah lingkungan."
                "lingkungan" -> "Kunjungi Bank Sampah terdekat dan tukarkan 1kg botol plastik."
                else -> ""
            }

            // Deadline
            itemView.findViewById<TextView>(R.id.tvDeadline).text = when (kategori) {
                "laporan" -> "Hingga 24 Des"
                "transportasi" -> "Sisa 2 jam"
                else -> "Harian"
            }

            val btnSelesaikan = itemView.findViewById<Button>(R.id.btnSelesaikan)

            if (sudahSelesai) {
                btnSelesaikan.text = "✓ SELESAI"
                btnSelesaikan.isEnabled = false
                btnSelesaikan.alpha = 0.5f
            } else {
                btnSelesaikan.setOnClickListener {
                    val safeCtx = context ?: return@setOnClickListener  // ← lebih aman
                    when (kategori) {
                        "laporan" -> {
                            startActivity(Intent(safeCtx, LaporanJalanActivity::class.java).apply {
                                putExtra("misi_id", misiId)
                                putExtra("poin", misi["poin"]?.toInt() ?: 50)
                                putExtra("judul", misi["judul"])
                                putExtra("lokasi", misi["lokasi"])
                                putExtra("kategori", misi["kategori"])
                            })
                        }
                        "transportasi", "lingkungan" -> {
                            startActivity(Intent(safeCtx, ScanQrActivity::class.java).apply {
                                putExtra("misi_id", misiId)
                                putExtra("poin", misi["poin"]?.toInt() ?: 30)
                                putExtra("judul", misi["judul"])
                            })
                        }
                    }
                }
            }
            container.addView(itemView)
        }
    }
}