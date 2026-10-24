package io.github.smouj.mydevwatch.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.smouj.mydevwatch.ble.ScannedDevice
import io.github.smouj.mydevwatch.model.DeviceProfile
import io.github.smouj.mydevwatch.protocol.ConnectionState
import java.time.Instant

/**
 * Single observable state for the shell.
 *
 * It intentionally holds no health data when no watch is connected: an empty
 * dashboard is honest, a demo number is not. [previewMode] exists so designers can
 * see the layout with clearly labelled sample values.
 */
class AppState {
    var connection by mutableStateOf(ConnectionState.IDLE)
    var devices by mutableStateOf<List<ScannedDevice>>(emptyList())
    var profile by mutableStateOf<DeviceProfile?>(null)
    var battery by mutableStateOf<Int?>(null)
    var lastSync by mutableStateOf<Instant?>(null)
    var previewMode by mutableStateOf(false)
    var settingsVisible by mutableStateOf(false)
    var scanning by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)

    val connected: Boolean get() = connection == ConnectionState.READY || connection == ConnectionState.SYNCING

    fun reset() {
        connection = ConnectionState.IDLE
        profile = null
        battery = null
        lastSync = null
    }
}
