package com.example.smart_city.helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "smartcity.db"
        const val DATABASE_VERSION = 2

        // Tabel User
        const val TABLE_USER = "users"
        const val COL_ID = "id"
        const val COL_NAMA = "nama"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD = "password"
        const val COL_KOTA = "kota"
        const val COL_POIN = "poin"
        const val COL_LEVEL = "level"

        // Tabel Riwayat Poin
        const val TABLE_POIN = "riwayat_poin"
        const val COL_POIN_ID = "id"
        const val COL_POIN_USER_ID = "user_id"
        const val COL_POIN_JUMLAH = "jumlah"
        const val COL_POIN_KETERANGAN = "keterangan"
        const val COL_POIN_KATEGORI = "kategori"
        const val COL_POIN_TANGGAL = "tanggal"

        // Tabel Misi
        const val TABLE_MISI = "misi"
        const val COL_MISI_ID = "id"
        const val COL_MISI_JUDUL = "judul"
        const val COL_MISI_LOKASI = "lokasi"
        const val COL_MISI_POIN = "poin"
        const val COL_MISI_ICON = "icon"
        const val COL_MISI_WARNA = "warna"
        const val COL_MISI_KATEGORI = "kategori"

        // Tabel Misi User (misi yang sudah diselesaikan)
        const val TABLE_MISI_USER = "misi_user"
        const val COL_MU_ID = "id"
        const val COL_MU_USER_ID = "user_id"
        const val COL_MU_MISI_ID = "misi_id"
        const val COL_MU_TANGGAL = "tanggal"

        // Tabel Laporan
        const val TABLE_LAPORAN = "laporan"
        const val COL_LAP_ID = "id"
        const val COL_LAP_USER_ID = "user_id"
        const val COL_LAP_JUDUL = "judul"
        const val COL_LAP_CATATAN = "catatan"
        const val COL_LAP_FOTO = "foto_path"
        const val COL_LAP_LOKASI = "lokasi"
        const val COL_LAP_LAT = "latitude"
        const val COL_LAP_LNG = "longitude"
        const val COL_LAP_TANGGAL = "tanggal"

        // Level berdasarkan poin
        fun getLevelFromPoin(poin: Int): String {
            return when {
                poin >= 5000 -> "City Hero"
                poin >= 3000 -> "Elite Contributor"
                poin >= 2000 -> "City Guardian"
                poin >= 1000 -> "Warga Aktif"
                poin >= 500  -> "Local Hero"
                else         -> "Warga Baru"
            }
        }

        // Target poin untuk level berikutnya
        fun getTargetPoin(poin: Int): Int {
            return when {
                poin >= 5000 -> 10000
                poin >= 3000 -> 5000
                poin >= 2000 -> 3000
                poin >= 1000 -> 2000
                poin >= 500  -> 1000
                else         -> 500
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tabel laporan
        db.execSQL("""
            CREATE TABLE $TABLE_LAPORAN (
                $COL_LAP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LAP_USER_ID INTEGER NOT NULL,
                $COL_LAP_JUDUL TEXT NOT NULL,
                $COL_LAP_CATATAN TEXT,
                $COL_LAP_FOTO TEXT,
                $COL_LAP_LOKASI TEXT,
                $COL_LAP_LAT REAL,
                $COL_LAP_LNG REAL,
                $COL_LAP_TANGGAL TEXT NOT NULL
             )
        """.trimIndent())

        // Tabel User
        db.execSQL("""
            CREATE TABLE $TABLE_USER (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAMA TEXT NOT NULL,
                $COL_EMAIL TEXT UNIQUE NOT NULL,
                $COL_PASSWORD TEXT NOT NULL,
                $COL_KOTA TEXT NOT NULL,
                $COL_POIN INTEGER DEFAULT 50,
                $COL_LEVEL TEXT DEFAULT 'Warga Baru'
            )
        """.trimIndent())

        // Tabel Riwayat Poin
        db.execSQL("""
            CREATE TABLE $TABLE_POIN (
                $COL_POIN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_POIN_USER_ID INTEGER NOT NULL,
                $COL_POIN_JUMLAH INTEGER NOT NULL,
                $COL_POIN_KETERANGAN TEXT NOT NULL,
                $COL_POIN_KATEGORI TEXT NOT NULL,
                $COL_POIN_TANGGAL TEXT NOT NULL,
                FOREIGN KEY($COL_POIN_USER_ID) REFERENCES $TABLE_USER($COL_ID)
            )
        """.trimIndent())

        // Tabel Misi
        db.execSQL("""
            CREATE TABLE $TABLE_MISI (
                $COL_MISI_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_MISI_JUDUL TEXT NOT NULL,
                $COL_MISI_LOKASI TEXT NOT NULL,
                $COL_MISI_POIN INTEGER NOT NULL,
                $COL_MISI_ICON TEXT NOT NULL,
                $COL_MISI_WARNA TEXT NOT NULL,
                $COL_MISI_KATEGORI TEXT NOT NULL
            )
        """.trimIndent())

        // Tabel Misi User
        db.execSQL("""
            CREATE TABLE $TABLE_MISI_USER (
                $COL_MU_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_MU_USER_ID INTEGER NOT NULL,
                $COL_MU_MISI_ID INTEGER NOT NULL,
                $COL_MU_TANGGAL TEXT NOT NULL,
                FOREIGN KEY($COL_MU_USER_ID) REFERENCES $TABLE_USER($COL_ID),
                FOREIGN KEY($COL_MU_MISI_ID) REFERENCES $TABLE_MISI($COL_MISI_ID)
            )
        """.trimIndent())

        // Insert misi default
        insertDefaultMisi(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MISI_USER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POIN")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MISI")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    private fun insertDefaultMisi(db: SQLiteDatabase) {
        val misiList = listOf(
            arrayOf("Laporkan Jalan Berlubang", "Wilayah Medan Baru", "50", "🚧", "#E8541A", "laporan"),
            arrayOf("Gunakan Trans Metro", "Halte Trans Metro Medan", "30", "🚌", "#1A6B5A", "transportasi"),
            arrayOf("Setor Sampah Plastik", "Bank Sampah Terdekat", "100", "♻️", "#2D8B70", "lingkungan")
        )
        misiList.forEach {
            db.execSQL("""
            INSERT INTO $TABLE_MISI
            ($COL_MISI_JUDUL, $COL_MISI_LOKASI, $COL_MISI_POIN, $COL_MISI_ICON, $COL_MISI_WARNA, $COL_MISI_KATEGORI)
            VALUES ('${it[0]}', '${it[1]}', ${it[2]}, '${it[3]}', '${it[4]}', '${it[5]}')
        """.trimIndent())
        }
    }

    // ==================== USER ====================

    fun registerUser(nama: String, email: String, password: String, kota: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAMA, nama)
            put(COL_EMAIL, email)
            put(COL_PASSWORD, password)
            put(COL_KOTA, kota)
            put(COL_POIN, 50) // bonus poin pertama
            put(COL_LEVEL, "Warga Baru")
        }
        val result = db.insert(TABLE_USER, null, values)
        db.close()
        return result != -1L
    }

    fun isEmailExist(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(TABLE_USER, arrayOf(COL_ID),
            "$COL_EMAIL = ?", arrayOf(email), null, null, null)
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun loginUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(TABLE_USER, arrayOf(COL_ID),
            "$COL_EMAIL = ? AND $COL_PASSWORD = ?",
            arrayOf(email, password), null, null, null)
        val valid = cursor.count > 0
        cursor.close()
        db.close()
        return valid
    }

    fun getUserByEmail(email: String): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.query(TABLE_USER, null,
            "$COL_EMAIL = ?", arrayOf(email), null, null, null)
        return if (cursor.moveToFirst()) {
            val data = mapOf(
                "id"     to cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)),
                "nama"   to cursor.getString(cursor.getColumnIndexOrThrow(COL_NAMA)),
                "email"  to cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                "kota"   to cursor.getString(cursor.getColumnIndexOrThrow(COL_KOTA)),
                "poin"   to cursor.getString(cursor.getColumnIndexOrThrow(COL_POIN)),
                "level"  to cursor.getString(cursor.getColumnIndexOrThrow(COL_LEVEL))
            )
            cursor.close()
            db.close()
            data
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    fun getUserById(userId: Int): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.query(TABLE_USER, null,
            "$COL_ID = ?", arrayOf(userId.toString()), null, null, null)
        return if (cursor.moveToFirst()) {
            val data = mapOf(
                "id"    to cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)),
                "nama"  to cursor.getString(cursor.getColumnIndexOrThrow(COL_NAMA)),
                "email" to cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                "kota"  to cursor.getString(cursor.getColumnIndexOrThrow(COL_KOTA)),
                "poin"  to cursor.getString(cursor.getColumnIndexOrThrow(COL_POIN)),
                "level" to cursor.getString(cursor.getColumnIndexOrThrow(COL_LEVEL))
            )
            cursor.close()
            db.close()
            data
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    // ==================== POIN ====================

    fun tambahPoin(userId: Int, jumlah: Int, keterangan: String, kategori: String): Boolean {
        val db = writableDatabase

        // Tambah ke riwayat
        val tanggal = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault()).format(java.util.Date())
        val riwayat = ContentValues().apply {
            put(COL_POIN_USER_ID, userId)
            put(COL_POIN_JUMLAH, jumlah)
            put(COL_POIN_KETERANGAN, keterangan)
            put(COL_POIN_KATEGORI, kategori)
            put(COL_POIN_TANGGAL, tanggal)
        }
        db.insert(TABLE_POIN, null, riwayat)

        // Update total poin user
        db.execSQL("""
            UPDATE $TABLE_USER 
            SET $COL_POIN = $COL_POIN + $jumlah,
                $COL_LEVEL = CASE
                    WHEN $COL_POIN + $jumlah >= 5000 THEN 'City Hero'
                    WHEN $COL_POIN + $jumlah >= 3000 THEN 'Elite Contributor'
                    WHEN $COL_POIN + $jumlah >= 2000 THEN 'City Guardian'
                    WHEN $COL_POIN + $jumlah >= 1000 THEN 'Warga Aktif'
                    WHEN $COL_POIN + $jumlah >= 500  THEN 'Local Hero'
                    ELSE 'Warga Baru'
                END
            WHERE $COL_ID = $userId
        """.trimIndent())

        db.close()
        return true
    }

    // ==================== MISI ====================

    fun getMisiHariIni(filter: String = "semua"): List<Map<String, String>> {
        val db = readableDatabase
        val where = if (filter == "semua") null else "$COL_MISI_KATEGORI = ?"
        val args = if (filter == "semua") null else arrayOf(filter)
        val cursor = db.query(TABLE_MISI, null, where, args, null, null, null)
        val list = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            list.add(mapOf(
                "id"       to cursor.getString(cursor.getColumnIndexOrThrow(COL_MISI_ID)),
                "judul"    to cursor.getString(cursor.getColumnIndexOrThrow(COL_MISI_JUDUL)),
                "lokasi"   to cursor.getString(cursor.getColumnIndexOrThrow(COL_MISI_LOKASI)),
                "poin"     to cursor.getString(cursor.getColumnIndexOrThrow(COL_MISI_POIN)),
                "icon"     to cursor.getString(cursor.getColumnIndexOrThrow(COL_MISI_ICON)),
                "warna"    to cursor.getString(cursor.getColumnIndexOrThrow(COL_MISI_WARNA)),
                "kategori" to cursor.getString(cursor.getColumnIndexOrThrow(COL_MISI_KATEGORI))
            ))
        }
        cursor.close()
        db.close()
        return list
    }

    fun isMisiSelesai(userId: Int, misiId: Int): Boolean {
        val db = readableDatabase
        // Cek apakah sudah diselesaikan hari ini
        val today = java.text.SimpleDateFormat("yyyy-MM-dd",
            java.util.Locale.getDefault()).format(java.util.Date())
        val cursor = db.query(TABLE_MISI_USER, arrayOf(COL_MU_ID),
            "$COL_MU_USER_ID = ? AND $COL_MU_MISI_ID = ? AND $COL_MU_TANGGAL LIKE ?",
            arrayOf(userId.toString(), misiId.toString(), "$today%"),
            null, null, null)
        val selesai = cursor.count > 0
        cursor.close()
        db.close()
        return selesai
    }

    fun selesaikanMisi(userId: Int, misiId: Int): Int {
        // Cek sudah selesai belum
        if (isMisiSelesai(userId, misiId)) return -1

        val db = writableDatabase
        val tanggal = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault()).format(java.util.Date())

        // Simpan misi selesai
        val values = ContentValues().apply {
            put(COL_MU_USER_ID, userId)
            put(COL_MU_MISI_ID, misiId)
            put(COL_MU_TANGGAL, tanggal)
        }
        db.insert(TABLE_MISI_USER, null, values)
        db.close()

        // Ambil poin misi
        val misiDb = readableDatabase
        val cursor = misiDb.query(TABLE_MISI, arrayOf(COL_MISI_POIN, COL_MISI_JUDUL),
            "$COL_MISI_ID = ?", arrayOf(misiId.toString()), null, null, null)
        var poin = 0
        var judul = ""
        if (cursor.moveToFirst()) {
            poin = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MISI_POIN))
            judul = cursor.getString(cursor.getColumnIndexOrThrow(COL_MISI_JUDUL))
        }
        cursor.close()
        misiDb.close()

        // Tambah poin ke user
        if (poin > 0) tambahPoin(userId, poin, "Misi: $judul", "misi")

        return poin
    }

    // ==================== LEADERBOARD ====================

    fun getTopWarga(limit: Int = 3): List<Map<String, String>> {
        val dummy = listOf(
            mapOf("nama" to "Andi Wijaya",   "poin" to "3450", "level" to "ELITE CONTRIBUTOR"),
            mapOf("nama" to "Siti Aminah",   "poin" to "2980", "level" to "CITY GUARDIAN"),
            mapOf("nama" to "Doni Pratama",  "poin" to "2710", "level" to "LOCAL HERO")
        )
        return dummy.take(limit)
    }

    fun simpanLaporan(
        userId: Int, judul: String, catatan: String,
        fotoPath: String, lokasi: String, lat: Double, lng: Double
    ): Boolean {
        val db = writableDatabase
        val tanggal = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault()).format(java.util.Date())
        val values = ContentValues().apply {
            put(COL_LAP_USER_ID, userId)
            put(COL_LAP_JUDUL, judul)
            put(COL_LAP_CATATAN, catatan)
            put(COL_LAP_FOTO, fotoPath)
            put(COL_LAP_LOKASI, lokasi)
            put(COL_LAP_LAT, lat)
            put(COL_LAP_LNG, lng)
            put(COL_LAP_TANGGAL, tanggal)
        }
        val result = db.insert(TABLE_LAPORAN, null, values)
        db.close()
        return result != -1L
    }
}