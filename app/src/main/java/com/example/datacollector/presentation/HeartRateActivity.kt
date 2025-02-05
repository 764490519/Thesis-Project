package com.example.datacollector.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.datacollector.presentation.theme.DataCollectorTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class HeartRateActivity : ComponentActivity(){

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
        super.onCreate(savedInstanceState)
        Log.i("HeartRateActivityUserDebug", "HeartRateActivity onCreate")

//        check Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request Permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BODY_SENSORS),
                1001
            )
        }

        val healthClient = HealthServices.getClient(this /*context*/)
        measureClient = healthClient.measureClient


        var supportsHeartRate = false;

        Log.d("TEST", "TEST...........")

        lifecycleScope.launch {
            val capabilities = measureClient.getCapabilitiesAsync().await()
            supportsHeartRate = DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure

            measureClient.registerMeasureCallback(
                DataType.Companion.HEART_RATE_BPM,
                heartRateCallback
            )

            setContent {
                WearApp(supportsHeartRate, heartRateFlow)
            }
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
}