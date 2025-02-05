/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.datacollector.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.datacollector.presentation.theme.DataCollectorTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout

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


        Log.i("MainActivityUserDebug","MainActivity onCreate")

        val rootLayout = LinearLayout(this)
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.gravity = Gravity.CENTER

        val bleScanButton = Button(this)
        bleScanButton.text = "scan BLE devices"
        bleScanButton.setTextColor(Color.BLUE)


        val heartrateButton = Button(this)
        heartrateButton.text = "measure HeartRate"
        heartrateButton.setTextColor(Color.RED)

        rootLayout.addView(bleScanButton)
        rootLayout.addView(heartrateButton)

        setContentView(rootLayout)

        bleScanButton.setOnClickListener {
            Log.i("MainActivityUserDebug","BLE Button Clicked")
            bleScanButton.text = "CLICKED!!!"
            val intent = Intent(this, BLEScanActivity::class.java)
            startActivity(intent)
        }

        heartrateButton.setOnClickListener {
            Log.i("MainActivityUserDebug","HeartRate Button Clicked")
            heartrateButton.text = "CLICKED!!!"
            val intent = Intent(this, HeartRateActivity::class.java)
            startActivity(intent)
        }





//        check Permission
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Request Permission
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.BODY_SENSORS),
//                1001
//            )
//        }
//
//
//        val healthClient = HealthServices.getClient(this /*context*/)
//        measureClient = healthClient.measureClient
//
//
//        var supportsHeartRate = false;
//
//        Log.d("TEST", "TEST...........")
//
//        lifecycleScope.launch {
//            val capabilities = measureClient.getCapabilitiesAsync().await()
//            supportsHeartRate = DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure
//
//            measureClient.registerMeasureCallback(
//                DataType.Companion.HEART_RATE_BPM,
//                heartRateCallback
//            )
//
//            setContent {
//                WearApp(supportsHeartRate, heartRateFlow)
//            }
        }
    }


    @Composable
    fun WearApp(supportsHeartRate: Boolean, heartRateFlow: StateFlow<Double?>) {

        val heartRate by heartRateFlow.collectAsState()

        DataCollectorTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                contentAlignment = Alignment.Center
            ) {
                if (supportsHeartRate) {
                    if (heartRate != null) {
                        Text(text = "Heart Rate: ${heartRate!!.toInt()} BPM")
                    } else {
                        Text(text = "Measuring Heart Rate...")
                    }
                } else {
                    Text(text = "Heart Rate NOT Supported!")
                }
            }
        }
    }


//@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    WearApp(false)
//}