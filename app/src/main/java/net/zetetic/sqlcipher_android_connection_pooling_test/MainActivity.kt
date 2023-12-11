package net.zetetic.sqlcipher_android_connection_pooling_test

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.zetetic.sqlcipher_android_connection_pooling_test.ui.theme.SqlcipherandroidconnectionpoolingtestTheme
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : ComponentActivity() {

    private var REBUILT_COUNT = 0;
    private val READERS = 10;
    private val MAX_DATABASE_SIZE_IN_BYTES = 1024 * 1000 * 500;
    private var threadPool: ExecutorService = Executors.newWorkStealingPool(READERS + 1);

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SqlcipherandroidconnectionpoolingtestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        System.loadLibrary("sqlcipher")
        val password = "Password1!"
        val databaseFile = getDatabasePath("demo.db")
        if(databaseFile.exists()){
            databaseFile.delete()
        }
        var helper = DatabaseHelper(this, databaseFile, password.toByteArray())
        val startBarrier = CyclicBarrier(READERS + 1)
        val stopBarrier = CyclicBarrier(READERS + 1)
        Log.i(javaClass.simpleName, "Building database...")
        while(true) {
            if(databaseFile.length() >= MAX_DATABASE_SIZE_IN_BYTES){
                REBUILT_COUNT++
                Log.i(javaClass.simpleName, "Database file size too large, database rebuilt $REBUILT_COUNT time(s)...")
                helper.close()
                databaseFile.delete()
                Writer.TOTAL_DATA_SIZE_IN_BYTES = 0
                helper = DatabaseHelper(this, databaseFile, password.toByteArray())
            }
            for(i in (1..READERS)){
                threadPool.execute(Reader(startBarrier, stopBarrier, helper))
            }
            threadPool.execute(Writer(startBarrier, helper))
            stopBarrier.await()
            Log.i(javaClass.simpleName, "All work completed!")
            startBarrier.reset()
            stopBarrier.reset()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SqlcipherandroidconnectionpoolingtestTheme {
        Greeting("Android")
    }
}