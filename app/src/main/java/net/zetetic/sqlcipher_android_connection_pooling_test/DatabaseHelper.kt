package net.zetetic.sqlcipher_android_connection_pooling_test

import android.content.Context
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import java.io.File

class DatabaseHelper (
    context: Context,
    databaseFile: File,
    password: ByteArray
) : SQLiteOpenHelper(context, databaseFile.absolutePath, password,
    null, 1, 1, null,
    null, false) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS data(id, info);")
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
}