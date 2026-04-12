package com.example.smart_city.helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "smartcity.db"
        const val DATABASE_VERSION = 1

        // Tabel User
        const val TABLE_USER = "users"
        const val COL_ID = "id"
        const val COL_NAMA = "nama"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD = "password"
        const val COL_KOTA = "kota"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USER (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAMA TEXT NOT NULL,
                $COL_EMAIL TEXT UNIQUE NOT NULL,
                $COL_PASSWORD TEXT NOT NULL,
                $COL_KOTA TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    // Register user baru
    fun registerUser(nama: String, email: String, password: String, kota: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAMA, nama)
            put(COL_EMAIL, email)
            put(COL_PASSWORD, password)
            put(COL_KOTA, kota)
        }
        val result = db.insert(TABLE_USER, null, values)
        db.close()
        return result != -1L
    }

    // Cek email sudah terdaftar belum
    fun isEmailExist(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER,
            arrayOf(COL_ID),
            "$COL_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Login user
    fun loginUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER,
            arrayOf(COL_ID),
            "$COL_EMAIL = ? AND $COL_PASSWORD = ?",
            arrayOf(email, password),
            null, null, null
        )
        val valid = cursor.count > 0
        cursor.close()
        db.close()
        return valid
    }

    // Ambil data user by email
    fun getUserByEmail(email: String): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER,
            null,
            "$COL_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            val data = mapOf(
                "id" to cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)),
                "nama" to cursor.getString(cursor.getColumnIndexOrThrow(COL_NAMA)),
                "email" to cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                "kota" to cursor.getString(cursor.getColumnIndexOrThrow(COL_KOTA))
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
}