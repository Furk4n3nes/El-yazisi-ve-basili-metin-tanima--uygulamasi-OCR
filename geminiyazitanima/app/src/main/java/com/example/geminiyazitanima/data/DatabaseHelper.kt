package com.example.geminiyazitanima.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Geçmiş kayıt veri modeli
data class HistoryItem(
    val id: Int,
    val title: String,
    val date: String,
    val time: String,
    val type: String // "PDF" veya "TXT"
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDB"
        private const val DATABASE_VERSION = 2
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        // History tablosu
        private const val TABLE_HISTORY = "history"
        private const val COLUMN_HISTORY_ID = "id"
        private const val COLUMN_HISTORY_TITLE = "title"
        private const val COLUMN_HISTORY_DATE = "date"
        private const val COLUMN_HISTORY_TIME = "time"
        private const val COLUMN_HISTORY_TYPE = "type"
        private const val COLUMN_HISTORY_USERNAME = "username"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USERNAME TEXT UNIQUE, " +
                "$COLUMN_PASSWORD TEXT)"
        db.execSQL(createTable)
        val createHistoryTable = "CREATE TABLE $TABLE_HISTORY (" +
                "$COLUMN_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_HISTORY_TITLE TEXT, " +
                "$COLUMN_HISTORY_DATE TEXT, " +
                "$COLUMN_HISTORY_TIME TEXT, " +
                "$COLUMN_HISTORY_TYPE TEXT, " +
                "$COLUMN_HISTORY_USERNAME TEXT)"
        db.execSQL(createHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }

    fun addUser(username: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USERNAME, username)
        values.put(COLUMN_PASSWORD, password)

        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun checkUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun updatePassword(username: String, newPassword: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PASSWORD, newPassword)
        }
        val result = db.update(TABLE_USERS, values, "$COLUMN_USERNAME = ?", arrayOf(username))
        return result > 0
    }

    fun addHistory(title: String, date: String, time: String, type: String, username: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_HISTORY_TITLE, title)
            put(COLUMN_HISTORY_DATE, date)
            put(COLUMN_HISTORY_TIME, time)
            put(COLUMN_HISTORY_TYPE, type)
            put(COLUMN_HISTORY_USERNAME, username)
        }
        val result = db.insert(TABLE_HISTORY, null, values)
        return result != -1L
    }

    fun getAllHistory(username: String): List<HistoryItem> {
        val db = this.readableDatabase
        val list = mutableListOf<HistoryItem>()
        val cursor = db.query(
            TABLE_HISTORY,
            null,
            "$COLUMN_HISTORY_USERNAME = ?",
            arrayOf(username),
            null, null, "$COLUMN_HISTORY_ID DESC"
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_TITLE))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_DATE))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_TIME))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_TYPE))
                list.add(HistoryItem(id, title, date, time, type))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteHistory(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_HISTORY, "$COLUMN_HISTORY_ID = ?", arrayOf(id.toString()))
    }

    fun updateUserInfo(oldUsername: String, newUsername: String, newPassword: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, newUsername)
            put(COLUMN_PASSWORD, newPassword)
        }
        
        // Kullanıcı adı değiştiyse, history tablosundaki kayıtları da güncelle
        if (oldUsername != newUsername) {
            val historyValues = ContentValues().apply {
                put(COLUMN_HISTORY_USERNAME, newUsername)
            }
            db.update(TABLE_HISTORY, historyValues, "$COLUMN_HISTORY_USERNAME = ?", arrayOf(oldUsername))
        }
        
        val result = db.update(TABLE_USERS, values, "$COLUMN_USERNAME = ?", arrayOf(oldUsername))
        return result > 0
    }
} 