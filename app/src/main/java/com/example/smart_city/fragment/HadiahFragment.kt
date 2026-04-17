package com.example.smart_city.fragment

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.smart_city.R
import com.example.smart_city.helper.DatabaseHelper
import com.example.smart_city.helper.ToastHelper

class HadiahFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private var userId = 0
    private var filterAktif = "semua"
    private val handler = android.os.Handler(android.os.Looper.getMainLooper()) // ← tambah ini
    private var pendingRunnable: Runnable? = null // ← tambah ini

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hadiah, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())
        val sharedPref = requireActivity()
            .getSharedPreferences("smartcity_session", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", 0)

        loadData(view)

        // SwipeRefresh listener
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshHadiah)
        swipeRefresh.setOnRefreshListener {
            loadData(view)
        }
    }
    private fun loadData(view: View) {
        val skeleton = view.findViewById<View>(R.id.skeletonHadiah)
        val content = view.findViewById<View>(R.id.contentHadiah)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshHadiah)

            skeleton.visibility = View.VISIBLE
            content.visibility = View.GONE


        // Cancel runnable sebelumnya kalau masih pending
        pendingRunnable?.let { handler.removeCallbacks(it) }

        pendingRunnable = Runnable {
            if (!isAdded || context == null) return@Runnable

            skeleton.visibility = View.GONE
            content.visibility = View.VISIBLE
            swipeRefresh.isRefreshing = false

            setupFilter(view)
            loadSaldo(view)
            loadHadiah(view, filterAktif)
            loadRekomendasi(view)
        }
        handler.postDelayed(pendingRunnable!!, 400)
    }

    override fun onResume() {
        super.onResume()
        view?.let {
            if (it.findViewById<View>(R.id.contentHadiah).visibility == View.VISIBLE) {
                loadSaldo(it)
                loadHadiah(it, filterAktif)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        pendingRunnable?.let { handler.removeCallbacks(it) } // ← cancel handler
    }


    private fun setupFilter(view: View) {
        val tabSemua = view.findViewById<TextView>(R.id.tabSemuaHadiah)
        val tabVoucher = view.findViewById<TextView>(R.id.tabVoucher)
        val tabBadge = view.findViewById<TextView>(R.id.tabBadge)

        tabSemua.setOnClickListener {
            filterAktif = "semua"
            setActiveTab(tabSemua, listOf(tabVoucher, tabBadge))
            loadHadiah(view, filterAktif)
        }
        tabVoucher.setOnClickListener {
            filterAktif = "voucher"
            setActiveTab(tabVoucher, listOf(tabSemua, tabBadge))
            loadHadiah(view, filterAktif)
        }
        tabBadge.setOnClickListener {
            filterAktif = "badge"
            setActiveTab(tabBadge, listOf(tabSemua, tabVoucher))
            loadHadiah(view, filterAktif)
        }
    }

    private fun setActiveTab(active: TextView, inactive: List<TextView>) {
        active.setBackgroundResource(R.drawable.bg_tab_misi_active)
        active.setTextColor(Color.WHITE)
        inactive.forEach {
            it.setBackgroundResource(R.drawable.bg_tab_misi_inactive)
            it.setTextColor(Color.parseColor("#888888"))
        }
    }

    private fun loadSaldo(view: View) {
        val user = db.getUserById(userId) ?: return
        val poin = user["poin"]?.toInt() ?: 0
        view.findViewById<TextView>(R.id.tvSaldoPoin).text =
            "Poinmu: ${String.format("%,d", poin)} pts"
    }

    private fun loadHadiah(view: View, filter: String) {
        val container = view.findViewById<LinearLayout>(R.id.containerHadiah)
        container.removeAllViews()

        val hadiahList = db.getHadiah(filter)
        val user = db.getUserById(userId)
        val poinUser = user?.get("poin")?.toInt() ?: 0

        // Buat grid 2 kolom manual
        var row: LinearLayout? = null
        hadiahList.forEachIndexed { index, hadiah ->
            if (index % 2 == 0) {
                row = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 12.dpToPx() }
                    orientation = LinearLayout.HORIZONTAL
                }
                container.addView(row)
            }

            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_hadiah, null).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    ).also {
                        if (index % 2 == 0) it.marginEnd = 8.dpToPx()
                    }
                }

            val hadiahId = hadiah["id"]?.toInt() ?: return@forEachIndexed
            val poinHadiah = hadiah["poin"]?.toInt() ?: 0
            val cukupPoin = poinUser >= poinHadiah

            // Load gambar dengan Glide
            val ivHadiah = itemView.findViewById<ImageView>(R.id.ivHadiah)
            ivHadiah.setImageDrawable(null) // clear dulu sebelum load
            Glide.with(this)
                .load(hadiah["gambar_url"])
                .signature(com.bumptech.glide.signature.ObjectKey(hadiah["gambar_url"] ?: ""))
                .placeholder(R.drawable.bg_skeleton)
                .error(R.drawable.bg_skeleton)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .into(ivHadiah)

            // Set badge warna sesuai kategori
            val tvKategori = itemView.findViewById<TextView>(R.id.tvKategoriHadiah)
            tvKategori.text = hadiah["kategori"]?.uppercase()
            tvKategori.setBackgroundResource(
                when (hadiah["kategori"]) {
                    "badge" -> R.drawable.bg_badge_hadiah_teal
                    "promo" -> R.drawable.bg_badge_hadiah_green
                    else -> R.drawable.bg_badge_hadiah
                }
            )

            itemView.findViewById<TextView>(R.id.tvNamaHadiah).text = hadiah["nama"]
            itemView.findViewById<TextView>(R.id.tvPoinHadiah).text =
                "${String.format("%,d", poinHadiah)} pts"

            val btnTukar = itemView.findViewById<Button>(R.id.btnTukar)
            if (cukupPoin) {
                btnTukar.text = "Tukar"
                btnTukar.isEnabled = true
                btnTukar.alpha = 1f
                btnTukar.setOnClickListener {
                    showKonfirmasiTukar(hadiah["nama"] ?: "", hadiahId, poinHadiah, view)
                }
            } else {
                btnTukar.text = "Poin Kurang"
                btnTukar.isEnabled = false
                btnTukar.alpha = 0.5f
                btnTukar.setBackgroundResource(R.drawable.bg_button_disabled)
            }

            row?.addView(itemView)
        }

        // Kalau jumlah ganjil, tambah view kosong
        if (hadiahList.size % 2 != 0) {
            val emptyView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            row?.addView(emptyView)
        }
    }

    private fun loadRekomendasi(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.containerRekomendasi)
        container.removeAllViews()

        val rekList = db.getHadiahRekomendasi()
        rekList.forEach { hadiah ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_rekomendasi, container, false)

            val ivRek = itemView.findViewById<ImageView>(R.id.ivRekomendasiGambar)
            ivRek.setImageDrawable(null) // clear dulu
            Glide.with(this)
                .load(hadiah["gambar_url"])
                .signature(com.bumptech.glide.signature.ObjectKey(hadiah["gambar_url"] ?: ""))
                .apply(RequestOptions().transform(RoundedCorners(12)))
                .placeholder(R.drawable.bg_skeleton)
                .error(R.drawable.bg_skeleton)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .into(ivRek)

            itemView.findViewById<TextView>(R.id.tvRekNama).text = hadiah["nama"]
            itemView.findViewById<TextView>(R.id.tvRekPoin).text =
                "${String.format("%,d", hadiah["poin"]?.toInt() ?: 0)} pts"

            container.addView(itemView)
        }
    }

    private fun showKonfirmasiTukar(
        namaHadiah: String, hadiahId: Int, poin: Int, view: View
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Tukar Hadiah")
            .setMessage("Tukar \"$namaHadiah\" dengan ${String.format("%,d", poin)} poin?")
            .setPositiveButton("Tukar") { _, _ ->
                val berhasil = db.tukarHadiah(userId, hadiahId, poin)
                if (berhasil) {
                    ToastHelper.showSuccess(requireContext(),
                        "🎉 Berhasil menukar $namaHadiah!")
                    loadSaldo(view)
                    loadHadiah(view, filterAktif)
                } else {
                    ToastHelper.showError(requireContext(), "Poin tidak cukup!")
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}