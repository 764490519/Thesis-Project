package com.example.datacollector.presentation.Bluetooth

import java.util.UUID

object DeviceCharacteristics {
    //GENERIC_ACCESS
    val DEVICE_NAME = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
    val APPEARANCE = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb")
    val CONNECTION_PARAMETERS = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb")
    val CENTRAL_ADDRESS_RESOLUTION = UUID.fromString("00002aa6-0000-1000-8000-00805f9b34fb")

    //GENERIC_ATTRIBUTE
    val SERVICE_CHANGED = UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb")

    //DEVICE_INFORMATION
    val MANUFACTURER_NAME = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")
    val SERIAL_NUMBER = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb")

    //BATTERY_SERVICE
    val BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")

    //MOVESENSE_SERVICE
    val MOVESENSE_DATA_1 = UUID.fromString("6b200002-ff4e-4979-8186-fb7ba486fcd7")
    val MOVESENSE_DATA_2 = UUID.fromString("6b200001-ff4e-4979-8186-fb7ba486fcd7")

    private val CHARACTERISTIC_MAP = mapOf(
        DEVICE_NAME to "Device Name",
        APPEARANCE to "Appearance",
        CONNECTION_PARAMETERS to "Connection Parameters",
        CENTRAL_ADDRESS_RESOLUTION to "Central Address Resolution",
        MANUFACTURER_NAME to "Manufacturer Name",
        SERIAL_NUMBER to "Serial Number",
        BATTERY_LEVEL to "Battery Level",
        SERVICE_CHANGED to "Service Changed",
        MOVESENSE_DATA_1 to "MoveSense Data Stream 1",
        MOVESENSE_DATA_2 to "MoveSense Data Stream 2"
    )

    fun getCharacteristicName(uuid: UUID): String? = CHARACTERISTIC_MAP[uuid]
}