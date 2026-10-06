package io.github.antoniomancebo.openbrush.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context

class OralBBleScanner(
    context: Context,
    private val onAdvertisement: (BrushAdvertisement) -> Unit,
    private val onError: (String) -> Unit,
) {
    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    private var scanning = false

    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val bytes = result.scanRecord
                ?.getManufacturerSpecificData(OralBAdvertisementParser.ORAL_B_MANUFACTURER_ID)
                ?: return
            OralBAdvertisementParser.parse(bytes, result.rssi)?.let(onAdvertisement)
        }

        override fun onScanFailed(errorCode: Int) {
            scanning = false
            onError("Error de escaneo Bluetooth: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (scanning) return
        val adapter = bluetoothAdapter ?: run {
            onError("Este dispositivo no tiene Bluetooth disponible")
            return
        }
        if (!adapter.isEnabled) {
            onError("Activa Bluetooth para detectar el cepillo")
            return
        }
        val scanner = adapter.bluetoothLeScanner ?: run {
            onError("No se ha podido iniciar el escáner BLE")
            return
        }
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner.startScan(null, settings, callback)
        scanning = true
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        if (!scanning) return
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(callback)
        scanning = false
    }
}
