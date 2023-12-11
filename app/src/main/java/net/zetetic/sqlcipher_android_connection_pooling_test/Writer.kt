package net.zetetic.sqlcipher_android_connection_pooling_test

import org.apache.commons.io.FileUtils
import android.util.Log
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.concurrent.CyclicBarrier
import kotlin.random.Random


class Writer(
    private val startBarrier: CyclicBarrier,
    private val helper: DatabaseHelper
) : Runnable {

    companion object {
        const val ROWS = 100
        const val ROW_SIZE_IN_BYTES = 512
        var ROW_START_INDEX = -(ROWS)
        var ROW_STOP_INDEX = 0
        var TOTAL_DATA_SIZE_IN_BYTES = 0L
    }

    init {
        ROW_START_INDEX += ROWS
        ROW_STOP_INDEX += ROWS
    }

    override fun run() {
        Log.i(javaClass.simpleName, "Writing rows $ROW_START_INDEX to $ROW_STOP_INDEX from thread ${Thread.currentThread().id}...")
        val database = helper.writableDatabase
        try {
            database.beginTransaction()
            var dataSize = 0L
            for(row in (ROW_START_INDEX..ROW_STOP_INDEX)){
                val data = ByteArray(ROW_SIZE_IN_BYTES);
                Random.nextBytes(data)
                database.execSQL("INSERT INTO DATA(id, info) VALUES(?, ?);", arrayOf(row, data))
                dataSize += ROW_SIZE_IN_BYTES
            }
            database.setTransactionSuccessful()
            TOTAL_DATA_SIZE_IN_BYTES += dataSize;
        } catch (ex: Exception){
            Log.i(javaClass.simpleName, "Failed to insert data", ex)
        }
        finally {
            database.endTransaction()
        }
        val displayTotalSize = FileUtils.byteCountToDisplaySize(TOTAL_DATA_SIZE_IN_BYTES)
        Log.i(javaClass.simpleName, "Done writing $ROWS rows of data ($displayTotalSize written in total) from thread ${Thread.currentThread().id}...")
        startBarrier.await()
    }
}