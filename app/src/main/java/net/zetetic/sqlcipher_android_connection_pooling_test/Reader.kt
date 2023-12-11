package net.zetetic.sqlcipher_android_connection_pooling_test

import android.util.Log
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.util.concurrent.CyclicBarrier

class Reader(
    private val startBarrier: CyclicBarrier,
    private val stopBarrier: CyclicBarrier,
    private val helper: DatabaseHelper
) : Runnable {
    override fun run() {
        startBarrier.await()
        val database = helper.writableDatabase
        Log.i(javaClass.simpleName, "Reading ${Writer.ROWS} rows (${Writer.ROW_START_INDEX}–${Writer.ROW_STOP_INDEX}) from thread ${Thread.currentThread().id}...")
        val cursor = database.query("SELECT * FROM DATA WHERE id between ? AND ?;",
            arrayOf(Writer.ROW_START_INDEX, Writer.ROW_STOP_INDEX))
        while (cursor != null && cursor.moveToNext()) {
            val idIndex = cursor.getColumnIndex("id")
            val infoIndex = cursor.getColumnIndex("info")
            val id = cursor.getInt(idIndex)
            val data = cursor.getBlob(infoIndex)
            //Log.i(javaClass.simpleName, "Found record for id $id data size ${data.size}")
        }
        cursor?.close()
        Log.i(javaClass.simpleName, "Done reading ${Writer.ROWS} rows of data from thread ${Thread.currentThread().id}...")
        stopBarrier.await()
    }
}