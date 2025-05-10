package com.example.datacollector.presentation.bluetooth_test

import java.util.UUID

object DeviceServices {
    val GENERIC_ACCESS = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
    val GENERIC_ATTRIBUTE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb")
    val DEVICE_INFORMATION = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
    val BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
    val MOVESENSE_SERVICE = UUID.fromString("0000fdf3-0000-1000-8000-00805f9b34fb")

    private val SERVICE_MAP = mapOf(
        GENERIC_ACCESS to "Generic Access",
        GENERIC_ATTRIBUTE to "Generic Attribute",
        DEVICE_INFORMATION to "Device Information",
        BATTERY_SERVICE to "Battery Service",
        MOVESENSE_SERVICE to "MoveSense Custom Service"
    )

    fun getServiceName(uuid: UUID): String? = SERVICE_MAP[uuid]
}