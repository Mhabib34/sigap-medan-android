package com.example.smart_city.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.smart_city.R
import com.example.smart_city.activity.LaporanJalanActivity
import com.example.smart_city.activity.ScanQrActivity
import com.example.smart_city.helper.DatabaseHelper
import com.example.smart_city.helper.ToastHelper

class BerandaFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private var userId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_beranda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())

        val sharedPref = requireActivity()
            .getSharedPreferences("smartcity_session", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", 0)

        //load data di beranda
        loadData(view)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        // Tampilkan skeleton dulu
        swipeRefresh.setOnRefreshListener {
            //load data di beranda
            loadData(view)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat kembali ke fragment
        view?.let {
            loadUserData(it)
            loadMisi(it)
            loadLeaderboard(it)
        }
    }

    private fun loadData(view: View){
        val skeleton = view.findViewById<View>(R.id.skeletonLayout)
        val content = view.findViewById<View>(R.id.contentLayout)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

        skeleton.visibility = View.VISIBLE
        content.visibility = View.GONE

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({

            loadUserData(view)
            loadMisi(view)
            loadLeaderboard(view)

            skeleton.visibility = View.GONE
            content.visibility = View.VISIBLE

            swipeRefresh.isRefreshing = false

        }, 500)
    }

    private fun loadUserData(view: View) {
        val user = db.getUserById(userId) ?: return
        val nama = user["nama"] ?: "User"
        val poin = user["poin"]?.toInt() ?: 0
        val level = user["level"] ?: "Warga Baru"
        val target = DatabaseHelper.getTargetPoin(poin)
        val sisaPoin = target - poin
        val progress = ((poin.toFloat() / target) * 100).toInt()

        // Header
        view.findViewById<TextView>(R.id.tvNama).text = "$nama!"
        view.findViewById<TextView>(R.id.tvAvatar).text = nama.take(2).uppercase()

        // Card status
        view.findViewById<TextView>(R.id.tvLevel).text = level
        view.findViewById<TextView>(R.id.tvPoin).text = String.format("%,d", poin)
        view.findViewById<TextView>(R.id.tvTarget).text = String.format("%,d Target", target)
        view.findViewById<TextView>(R.id.tvSisaPoin).text =
            "ⓘ $sisaPoin poin lagi untuk menjadi ${DatabaseHelper.getLevelFromPoin(poin + sisaPoin)}"
        view.findViewById<ProgressBar>(R.id.progressPoin).progress = progress
    }

    private fun loadMisi(view: View) {
        val misiList = db.getMisiHariIni()
        val container = view.findViewById<LinearLayout>(R.id.containerMisi)
        container.removeAllViews()

        misiList.forEach { misi ->
            val misiId = misi["id"]?.toInt() ?: return@forEach
            val sudahSelesai = db.isMisiSelesai(userId, misiId)
            val kategori = misi["kategori"] ?: ""

            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_misi, container, false)

            itemView.findViewById<TextView>(R.id.tvMisiIcon).text = misi["icon"]
            itemView.findViewById<TextView>(R.id.tvMisiJudul).text = misi["judul"]
            itemView.findViewById<TextView>(R.id.tvMisiLokasi).text = misi["lokasi"]
            itemView.findViewById<TextView>(R.id.tvMisiPoin).text = "+${misi["poin"]} poin"

            val btnSelesaikan = itemView.findViewById<Button>(R.id.btnSelesaikan)
            val warna = misi["warna"] ?: "#E8541A"

            itemView.findViewById<View>(R.id.viewStrip)
                .setBackgroundColor(Color.parseColor(warna))

            if (sudahSelesai) {
                btnSelesaikan.text = "✓ Selesai"
                btnSelesaikan.isEnabled = false
                btnSelesaikan.alpha = 0.5f
                btnSelesaikan.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.success)
                )
            } else {
                btnSelesaikan.text = "Selesaikan"
                btnSelesaikan.setBackgroundColor(Color.parseColor(warna))
                btnSelesaikan.setOnClickListener {
                    when (kategori) {
                        "laporan" -> {
                            val intent = Intent(requireContext(), LaporanJalanActivity::class.java)
                            intent.putExtra("misi_id", misiId)
                            intent.putExtra("poin", misi["poin"]?.toInt() ?: 50)
                            startActivity(intent)
                        }
                        "transportasi", "lingkungan" -> {
                            val intent = Intent(requireContext(), ScanQrActivity::class.java)
                            intent.putExtra("misi_id", misiId)
                            intent.putExtra("poin", misi["poin"]?.toInt() ?: 30)
                            intent.putExtra("judul", misi["judul"])
                            startActivity(intent)
                        }
                        else -> {
                            // Fallback kalau kategori tidak dikenali
                            val poinDapat = db.selesaikanMisi(userId, misiId)
                            if (poinDapat > 0) {
                                ToastHelper.showSuccess(requireContext(), "🎉 Misi selesai! +$poinDapat poin")
                                loadUserData(view)
                                loadMisi(view)
                                loadLeaderboard(view)
                            }
                        }
                    }
                }
            }

            container.addView(itemView)
        }
    }

    private fun loadLeaderboard(view: View) {
        val topWarga = db.getTopWarga(3)
        val container = view.findViewById<LinearLayout>(R.id.containerLeaderboard)
        container.removeAllViews()

        val rankColors = listOf("#F5C400", "#AAAAAA", "#CD7F32")
        val rankEmoji = listOf("🥇", "🥈", "🥉")

        topWarga.forEachIndexed { index, user ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_leaderboard, container, false)

            itemView.findViewById<TextView>(R.id.tvRank).apply {
                text = rankEmoji[index]
                setTextColor(Color.parseColor(rankColors[index]))
            }
            itemView.findViewById<TextView>(R.id.tvLeaderNama).text = user["nama"]
            itemView.findViewById<TextView>(R.id.tvLeaderLevel).text = user["level"]
            itemView.findViewById<TextView>(R.id.tvLeaderPoin).text =
                String.format("%,d", user["poin"]?.toInt() ?: 0)

            container.addView(itemView)
        }
    }
}