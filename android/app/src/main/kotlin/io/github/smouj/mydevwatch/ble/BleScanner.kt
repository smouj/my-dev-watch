package io.github.smouj.mydevwatch.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Thin, honest wrapper around the platform BLE scanner.
 *
 * It reports what the radio reports. Identification is not done here: the protocol
 * registry scores the fingerprint afterwards, so a device named "S10" is never
 * assumed to be a GloryFit watch.
 */
class ScannedDevice(
    val address: String,
    val name: String?,
    val rssi: Int,
    val serviceUuids: Set<String>
)

class BleScanner(private val context: Context) {

    private val manager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private var callback: ScanCallback? = null

    fun hasPermission(): Boolean {
        val required = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return required.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun start(onDevice: (ScannedDevice) -> Unit, onError: (String) -> Unit): Boolean {
        if (!hasPermission()) return false
        val scanner = manager?.adapter?.bluetoothLeScanner ?: return false
        stop()
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val record = result.scanRecord
                onDevice(
                    ScannedDevice(
                        address = result.device.address,
                        name = record?.deviceName ?: result.device.name,
                        rssi = result.rssi,
                        serviceUuids = record?.serviceUuids?.map { it.uuid.toString() }?.toSet() ?: emptySet()
                    )
                )
            }

            override fun onScanFailed(errorCode: Int) {
                onError("Scan failed with code " + errorCode)
            }
        }
        callback = scanCallback
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(null, settings, scanCallback)
        return true
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        val active = callback ?: return
        runCatching { manager?.adapter?.bluetoothLeScanner?.stopScan(active) }
        callback = null
    }
}
