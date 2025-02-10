/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.datacollector.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import kotlinx.coroutines.flow.MutableStateFlow
import android.content.Intent
import android.widget.Button
import com.example.datacollector.R
import com.example.datacollector.presentation.Bluetooth.BLEScanActivity

class MainActivity : ComponentActivity() {

    private lateinit var measureClient: MeasureClient

    // Store HeartRateFlow data
    private val heartRateFlow = MutableStateFlow<Double?>(null)

    // Callback function
    val heartRateCallback = object : MeasureCallback {

        override fun onAvailabilityChanged(
            dataType: DeltaDataType<*, *>,
            availability: Availability
        ) {
            if (availability is DataTypeAvailability) {
                Log.d("HeartRate", "Availability changed: $availability")
            }
        }

        override fun onDataReceived(data: DataPointContainer) {
            // Inspect data points.
            val heartRateData = data.getData(DataType.HEART_RATE_BPM)
            if (heartRateData.isNotEmpty()) {
                val latestHeartRate = heartRateData.lastOrNull()?.value ?: return
                heartRateFlow.value = latestHeartRate
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        Log.i("MainActivityUserDebug", "MainActivity onCreate")

        setContentView(R.layout.main_activity)


        val bleScanButton = findViewById<Button>(R.id.bleScanButton)
        val heartrateButton = findViewById<Button>(R.id.heartrateButton)


        bleScanButton.setOnClickListener {
            Log.i("MainActivityUserDebug", "BLE Button Clicked")
            val intent = Intent(this, BLEScanActivity::class.java)
            startActivity(intent)
        }

        heartrateButton.setOnClickListener {
            Log.i("MainActivityUserDebug", "HeartRate Button Clicked")
            val intent = Intent(this, HeartRateActivity::class.java)
            startActivity(intent)
        }


    }

    override onC
}



