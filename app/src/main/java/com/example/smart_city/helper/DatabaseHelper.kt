package com.example.smart_city.helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "smartcity.db"
        const val DATABASE_VERSION = 4

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

        // Tabel Hadiah
        const val TABLE_HADIAH = "hadiah"
        const val COL_HAD_ID = "id"
        const val COL_HAD_NAMA = "nama"
        const val COL_HAD_DESKRIPSI = "deskripsi"
        const val COL_HAD_POIN = "poin"
        const val COL_HAD_KATEGORI = "kategori"
        const val COL_HAD_GAMBAR_URL = "gambar_url"
        const val COL_HAD_STOK = "stok"

        // Tabel Penukaran
        const val TABLE_TUKAR = "penukaran"
        const val COL_TUK_ID = "id"
        const val COL_TUK_USER_ID = "user_id"
        const val COL_TUK_HADIAH_ID = "hadiah_id"
        const val COL_TUK_POIN = "poin_digunakan"
        const val COL_TUK_TANGGAL = "tanggal"

        // Tambah konstanta
        const val TABLE_BADGE = "badge"
        const val COL_BAD_ID = "id"
        const val COL_BAD_NAMA = "nama"
        const val COL_BAD_ICON = "icon"
        const val COL_BAD_WARNA = "warna"
        const val COL_BAD_SYARAT_KATEGORI = "syarat_kategori"
        const val COL_BAD_SYARAT_JUMLAH = "syarat_jumlah"

        // Tabel badge user (yang sudah dimiliki)
        const val TABLE_BADGE_USER = "badge_user"
        const val COL_BU_ID = "id"
        const val COL_BU_USER_ID = "user_id"
        const val COL_BU_BADGE_ID = "badge_id"
        const val COL_BU_TANGGAL = "tanggal"

        // Level berdasarkan poin
        fun getLevelFromPoin(poin: Int): String {
            return when {
                poin >= 500 -> "City Hero"
                poin >= 400 -> "Elite Contributor"
                poin >= 200 -> "City Guardian"
                poin >= 150 -> "Warga Aktif"
                poin >= 100  -> "Local Hero"
                else         -> "Warga Baru"
            }
        }

        // Target poin untuk level berikutnya
        fun getTargetPoin(poin: Int): Int {
            return when {
                poin >= 500 -> 1000
                poin >= 400 -> 500
                poin >= 200 -> 400
                poin >= 150 -> 200
                poin >= 100  -> 150
                else         -> 100
            }
        }
    }
    private fun insertDefaultHadiah(db: SQLiteDatabase) {
        val hadiahList = listOf(
            arrayOf("Voucher Mie Aceh Titi Bobrok", "Voucher makan gratis 1 porsi", "300",
                "voucher", "https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=300"),
            arrayOf("Kopi Kenangan 50% Disc", "Diskon 50% untuk semua menu", "150",
                "voucher", "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=300"),
            arrayOf("General Checkup RS Siloam", "Paket general checkup lengkap", "2500",
                "voucher", "https://images.unsplash.com/photo-1584820927498-cfe5211fd8bf?w=300"),
            arrayOf("Pahlawan Lingkungan Medan", "Badge eksklusif pecinta lingkungan", "500",
                "badge", "https://images.unsplash.com/photo-1719463814255-a7ee39b3630a?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8YmFkZ2V8ZW58MHx8MHx8fDA%3D"),
            arrayOf("Staycation 1 Malam JW Marriott", "Promo terbatas menginap 1 malam", "5000",
                "promo", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=300"),
            arrayOf("Voucher GrabFood 50rb", "Voucher diskon GrabFood", "400",
                "voucher", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=300")
        )
        hadiahList.forEach {
            db.execSQL("""
            INSERT INTO $TABLE_HADIAH
            ($COL_HAD_NAMA, $COL_HAD_DESKRIPSI, $COL_HAD_POIN, $COL_HAD_KATEGORI, $COL_HAD_GAMBAR_URL)
            VALUES ('${it[0]}', '${it[1]}', ${it[2]}, '${it[3]}', '${it[4]}')
        """.trimIndent())
        }
    }

    private fun insertDefaultBadge(db: SQLiteDatabase) {
        val badgeList = listOf(
            arrayOf("City Hero",     "🏅", "#FFF3EE", "laporan",       "1"),
            arrayOf("Eco Warrior",   "🌿", "#EEFAF5", "lingkungan",    "1"),
            arrayOf("Social Titan",  "🤝", "#F0F0F0", "sosial",        "3"),
            arrayOf("Road Master",   "🚌", "#FFFBE6", "transportasi",  "1")
        )
        badgeList.forEach {
            db.execSQL("""
            INSERT INTO $TABLE_BADGE
            ($COL_BAD_NAMA, $COL_BAD_ICON, $COL_BAD_WARNA, $COL_BAD_SYARAT_KATEGORI, $COL_BAD_SYARAT_JUMLAH)
            VALUES ('${it[0]}', '${it[1]}', '${it[2]}', '${it[3]}', ${it[4]})
        """.trimIndent())
        }
    }

    // Ambil semua badge + status unlock per user
    fun getBadgeUser(userId: Int): List<Map<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery("""
        SELECT b.*, 
               CASE WHEN bu.$COL_BU_USER_ID IS NOT NULL THEN '1' ELSE '0' END as unlocked
        FROM $TABLE_BADGE b
        LEFT JOIN $TABLE_BADGE_USER bu 
            ON b.$COL_BAD_ID = bu.$COL_BU_BADGE_ID 
            AND bu.$COL_BU_USER_ID = ?
    """, arrayOf(userId.toString()))

        val list = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            list.add(mapOf(
                "id"       to cursor.getString(cursor.getColumnIndexOrThrow(COL_BAD_ID)),
                "nama"     to cursor.getString(cursor.getColumnIndexOrThrow(COL_BAD_NAMA)),
                "icon"     to cursor.getString(cursor.getColumnIndexOrThrow(COL_BAD_ICON)),
                "warna"    to cursor.getString(cursor.getColumnIndexOrThrow(COL_BAD_WARNA)),
                "unlocked" to cursor.getString(cursor.getColumnIndexOrThrow("unlocked"))
            ))
        }
        cursor.close()
        db.close()
        return list
    }

    // Cek dan unlock badge otomatis setelah selesai misi
    fun cekDanUnlockBadge(userId: Int, kategoriMisi: String) {
        val jumlahSelesai = readableDatabase.use { db ->
            val c = db.rawQuery("""
            SELECT COUNT(*) FROM $TABLE_MISI_USER mu
            JOIN $TABLE_MISI m ON mu.$COL_MU_MISI_ID = m.$COL_MISI_ID
            WHERE mu.$COL_MU_USER_ID = ? AND m.$COL_MISI_KATEGORI = ?
        """, arrayOf(userId.toString(), kategoriMisi))
            c.moveToFirst()
            val count = c.getInt(0)
            c.close()
            count
        }

        val db = writableDatabase
        val cursor = db.query(TABLE_BADGE, arrayOf(COL_BAD_ID, COL_BAD_SYARAT_JUMLAH),
            "$COL_BAD_SYARAT_KATEGORI = ?", arrayOf(kategoriMisi), null, null, null)

        val tanggal = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault()).format(java.util.Date())

        while (cursor.moveToNext()) {
            val badgeId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BAD_ID))
            val syarat = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BAD_SYARAT_JUMLAH))

            if (jumlahSelesai >= syarat) {
                // Cek belum punya badge ini
                val cek = db.query(TABLE_BADGE_USER, arrayOf(COL_BU_ID),
                    "$COL_BU_USER_ID = ? AND $COL_BU_BADGE_ID = ?",
                    arrayOf(userId.toString(), badgeId.toString()), null, null, null)
                val belumPunya = cek.count == 0
                cek.close()

                if (belumPunya) {
                    val values = ContentValues().apply {
                        put(COL_BU_USER_ID, userId)
                        put(COL_BU_BADGE_ID, badgeId)
                        put(COL_BU_TANGGAL, tanggal)
                    }
                    db.insert(TABLE_BADGE_USER, null, values)
                }
            }
        }
        cursor.close()
        db.close()
    }


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_BADGE (
                $COL_BAD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_BAD_NAMA TEXT NOT NULL,
                $COL_BAD_ICON TEXT NOT NULL,
                $COL_BAD_WARNA TEXT NOT NULL,
                $COL_BAD_SYARAT_KATEGORI TEXT NOT NULL,
                $COL_BAD_SYARAT_JUMLAH INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_BADGE_USER (
                $COL_BU_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_BU_USER_ID INTEGER NOT NULL,
                $COL_BU_BADGE_ID INTEGER NOT NULL,
                $COL_BU_TANGGAL TEXT NOT NULL
            )
        """.trimIndent())

        insertDefaultBadge(db)
        // Tabel Hadiah
        db.execSQL("""
            CREATE TABLE $TABLE_HADIAH (
                $COL_HAD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_HAD_NAMA TEXT NOT NULL,
                $COL_HAD_DESKRIPSI TEXT,
                $COL_HAD_POIN INTEGER NOT NULL,
                $COL_HAD_KATEGORI TEXT NOT NULL,
                $COL_HAD_GAMBAR_URL TEXT,
                $COL_HAD_STOK INTEGER DEFAULT 10
            )
        """.trimIndent())

        // Tabel Penukaran
        db.execSQL("""
            CREATE TABLE $TABLE_TUKAR (
                $COL_TUK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TUK_USER_ID INTEGER NOT NULL,
                $COL_TUK_HADIAH_ID INTEGER NOT NULL,
                $COL_TUK_POIN INTEGER NOT NULL,
                $COL_TUK_TANGGAL TEXT NOT NULL
            )
        """.trimIndent())

        insertDefaultHadiah(db)

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
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BADGE_USER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BADGE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MISI_USER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POIN")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MISI")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HADIAH")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TUKAR")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LAPORAN")
        onCreate(db)
    }

    private fun insertDefaultMisi(db: SQLiteDatabase) {
        val misiList = listOf(
            // Misi lama
            arrayOf("Laporkan Jalan Berlubang", "Wilayah Medan Baru", "50", "🚧", "#E8541A", "laporan"),
            arrayOf("Laporkan Kemacetan", "Wilayah Medan Kota", "40", "🚥", "#E8541A", "laporan"),
            arrayOf("Gunakan Trans Metro", "Halte Trans Metro Medan", "30", "🚌", "#1A6B5A", "transportasi"),
            arrayOf("Setor Sampah Plastik", "Bank Sampah Terdekat", "100", "♻️", "#2D8B70", "lingkungan"),

            // Misi baru
            arrayOf("Laporkan Drainase Tersumbat", "Wilayah Medan Barat", "60", "🌊", "#E8541A", "laporan"),
            arrayOf("Laporkan TPS Overload", "TPS Terdekat di Medan", "55", "🗑️", "#E8541A", "laporan"),
            arrayOf("Laporkan Parkir Liar", "Wilayah Medan Kota", "45", "🚗", "#E8541A", "laporan"),
            arrayOf("Laporkan Lampu Jalan Mati", "Wilayah Medan Timur", "50", "💡", "#E8541A", "laporan")
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
                    WHEN $COL_POIN + $jumlah >= 500 THEN 'City Hero'
                    WHEN $COL_POIN + $jumlah >= 400 THEN 'Elite Contributor'
                    WHEN $COL_POIN + $jumlah >= 200 THEN 'City Guardian'
                    WHEN $COL_POIN + $jumlah >= 170 THEN 'Warga Aktif'
                    WHEN $COL_POIN + $jumlah >= 100 THEN 'Local Hero'
                    ELSE 'Warga Baru'
                END
            WHERE $COL_ID = $userId
        """.trimIndent())

        db.close()
        return true
    }

    fun getPoinUser(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER,
            arrayOf(COL_POIN),
            "$COL_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        val poin = if (cursor.moveToFirst())
            cursor.getInt(cursor.getColumnIndexOrThrow(COL_POIN))
        else 0
        cursor.close()
        db.close()
        return poin
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
        val cursor = misiDb.query(TABLE_MISI,
            arrayOf(COL_MISI_POIN, COL_MISI_JUDUL, COL_MISI_KATEGORI),  // ← tambah COL_MISI_KATEGORI
            "$COL_MISI_ID = ?", arrayOf(misiId.toString()), null, null, null)
        var poin = 0
        var judul = ""
        var kategoriMisi = ""
        if (cursor.moveToFirst()) {
            poin = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MISI_POIN))
            judul = cursor.getString(cursor.getColumnIndexOrThrow(COL_MISI_JUDUL))
            kategoriMisi = cursor.getString(cursor.getColumnIndexOrThrow(COL_MISI_KATEGORI))  // ← tambah
        }
        cursor.close()
        misiDb.close()

        // Tambah poin ke user
        if (poin > 0) {
            tambahPoin(userId, poin, "Misi: $judul", "misi")
            cekDanUnlockBadge(userId, kategoriMisi)
        }

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

    fun getHadiah(kategori: String = "semua"): List<Map<String, String>> {
        val db = readableDatabase
        val where = if (kategori == "semua") null else "$COL_HAD_KATEGORI = ?"
        val args = if (kategori == "semua") null else arrayOf(kategori)
        val cursor = db.query(TABLE_HADIAH, null, where, args, null, null, null)
        val list = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            list.add(mapOf(
                "id"         to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_ID)),
                "nama"       to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_NAMA)),
                "deskripsi"  to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_DESKRIPSI)),
                "poin"       to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_POIN)),
                "kategori"   to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_KATEGORI)),
                "gambar_url" to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_GAMBAR_URL)),
                "stok"       to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_STOK))
            ))
        }
        cursor.close()
        db.close()
        return list
    }

    fun tukarHadiah(userId: Int, hadiahId: Int, poinHadiah: Int): Boolean {
        val poinUser = getPoinUser(userId)
        if (poinUser < poinHadiah) return false

        val db = writableDatabase
        val tanggal = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault()).format(java.util.Date())

        // Simpan penukaran
        val values = ContentValues().apply {
            put(COL_TUK_USER_ID, userId)
            put(COL_TUK_HADIAH_ID, hadiahId)
            put(COL_TUK_POIN, poinHadiah)
            put(COL_TUK_TANGGAL, tanggal)
        }
        db.insert(TABLE_TUKAR, null, values)

        // Kurangi poin user
        db.execSQL("""
        UPDATE $TABLE_USER
        SET $COL_POIN = $COL_POIN - $poinHadiah
        WHERE $COL_ID = $userId
    """.trimIndent())

        db.close()
        return true
    }

    fun getHadiahRekomendasi(): List<Map<String, String>> {
        val db = readableDatabase
        val cursor = db.query(TABLE_HADIAH, null,
            "$COL_HAD_KATEGORI = ?", arrayOf("promo"),
            null, null, null)
        val list = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            list.add(mapOf(
                "id"         to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_ID)),
                "nama"       to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_NAMA)),
                "deskripsi"  to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_DESKRIPSI)),
                "poin"       to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_POIN)),
                "kategori"   to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_KATEGORI)),
                "gambar_url" to cursor.getString(cursor.getColumnIndexOrThrow(COL_HAD_GAMBAR_URL))
            ))
        }
        cursor.close()
        db.close()
        return list
    }

    fun getAllLaporan(): List<Map<String, String>> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LAPORAN,
            null,
            null, null, null, null,
            "$COL_LAP_TANGGAL DESC" // terbaru di atas
        )
        val list = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            list.add(
                mapOf(
                    "id"      to cursor.getString(cursor.getColumnIndexOrThrow(COL_LAP_ID)),
                    "user_id" to cursor.getString(cursor.getColumnIndexOrThrow(COL_LAP_USER_ID)),
                    "judul"   to cursor.getString(cursor.getColumnIndexOrThrow(COL_LAP_JUDUL)),
                    "catatan" to (cursor.getString(cursor.getColumnIndexOrThrow(COL_LAP_CATATAN)) ?: ""),
                    "foto"    to (cursor.getString(cursor.getColumnIndexOrThrow(COL_LAP_FOTO)) ?: ""),
                    "lokasi"  to (cursor.getString(cursor.getColumnIndexOrThrow(COL_LAP_LOKASI)) ?: ""),
                    "lat"     to cursor.getString(cursor.getColumnIndexOrThrow(COL_LAP_LAT)),
                    "lng"     to cursor.getString(cursor.getColumnIndexOrThrow(COL_LAP_LNG)),
                    "tanggal" to cursor.getString(cursor.getColumnIndexOrThrow(COL_LAP_TANGGAL))
                )
            )
        }
        cursor.close()
        db.close()
        return list
    }

    fun getLaporanHariIni(): Int {
        val db = readableDatabase
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LAPORAN WHERE $COL_LAP_TANGGAL LIKE ?",
            arrayOf("$today%")
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun getLaporanKemarin(): Int {
        val db = readableDatabase
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DATE, -1)
        val kemarin = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(cal.time)
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LAPORAN WHERE $COL_LAP_TANGGAL LIKE ?",
            arrayOf("$kemarin%")
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun getClusterStats(allReports: List<Map<String, String>>): Triple<Int, Int, Int> {
        // Konversi ke pair lat/lng
        val points = allReports.mapNotNull { row ->
            val lat = row["lat"]?.toDoubleOrNull() ?: return@mapNotNull null
            val lng = row["lng"]?.toDoubleOrNull() ?: return@mapNotNull null
            Pair(lat, lng)
        }

        val CLUSTER_RADIUS = 0.003
        val visited = BooleanArray(points.size)
        val clusterSizes = mutableListOf<Int>()

        for (i in points.indices) {
            if (visited[i]) continue
            var size = 1
            for (j in i + 1 until points.size) {
                if (!visited[j]) {
                    val dlat = points[i].first - points[j].first
                    val dlng = points[i].second - points[j].second
                    if (kotlin.math.sqrt(dlat * dlat + dlng * dlng) < CLUSTER_RADIUS) {
                        visited[j] = true
                        size++
                    }
                }
            }
            visited[i] = true
            clusterSizes.add(size)
        }

        val maxSize = clusterSizes.maxOrNull() ?: 1

        // Hitung threshold dinamis
        val thresholdKritis = (maxSize * 0.70).toInt().coerceAtLeast(2)
        val thresholdSedang = (maxSize * 0.35).toInt().coerceAtLeast(1)

        // Hitung jumlah klaster per tier
        val kritis  = clusterSizes.count { it >= thresholdKritis }
        val sedang  = clusterSizes.count { it in thresholdSedang until thresholdKritis }
        val rendah  = clusterSizes.count { it < thresholdSedang }

        return Triple(kritis, sedang, rendah)
    }

    fun getThresholds(allReports: List<Map<String, String>>): Pair<Int, Int> {
        val points = allReports.mapNotNull { row ->
            val lat = row["lat"]?.toDoubleOrNull() ?: return@mapNotNull null
            val lng = row["lng"]?.toDoubleOrNull() ?: return@mapNotNull null
            Pair(lat, lng)
        }

        val CLUSTER_RADIUS = 0.003
        val visited = BooleanArray(points.size)
        val clusterSizes = mutableListOf<Int>()

        for (i in points.indices) {
            if (visited[i]) continue
            var size = 1
            for (j in i + 1 until points.size) {
                if (!visited[j]) {
                    val dlat = points[i].first - points[j].first
                    val dlng = points[i].second - points[j].second
                    if (kotlin.math.sqrt(dlat * dlat + dlng * dlng) < CLUSTER_RADIUS) {
                        visited[j] = true
                        size++
                    }
                }
            }
            visited[i] = true
            clusterSizes.add(size)
        }

        val maxSize = clusterSizes.maxOrNull() ?: 1
        val thresholdKritis = (maxSize * 0.70).toInt().coerceAtLeast(2)
        val thresholdSedang = (maxSize * 0.35).toInt().coerceAtLeast(1)

        return Pair(thresholdKritis, thresholdSedang) // (kritis, sedang)
    }

    fun getAllMisi(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM misi ORDER BY id ASC", null
        )
        while (cursor.moveToNext()) {
            val map = mutableMapOf<String, String>()
            for (i in 0 until cursor.columnCount) {
                map[cursor.getColumnName(i)] = cursor.getString(i) ?: ""
            }
            list.add(map)
        }
        cursor.close()
        return list
    }
}