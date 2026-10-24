package io.github.smouj.mydevwatch.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.smouj.mydevwatch.R
import io.github.smouj.mydevwatch.ble.BleScanner
import io.github.smouj.mydevwatch.model.Capability
import io.github.smouj.mydevwatch.model.Confidence
import io.github.smouj.mydevwatch.model.DeviceProfile
import io.github.smouj.mydevwatch.model.SupportLevel
import io.github.smouj.mydevwatch.model.SupportStatus
import io.github.smouj.mydevwatch.protocol.ConnectionState
import io.github.smouj.mydevwatch.protocol.DeviceFingerprint
import io.github.smouj.mydevwatch.protocol.ProtocolRegistry
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    state: AppState,
    onOpenWatch: () -> Unit,
    onOpenCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scanner = remember { BleScanner(context) }
    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.all { granted -> granted }) {
            startScan(scanner, state)
        } else {
            state.message = "Bluetooth permission is required to look for watches."
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Your watch. Under your control.",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        "Local companion, face studio and BLE lab for GloryFit-family watches. No account, no telemetry.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Row(
                        modifier = Modifier.padding(top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(onClick = {
                            if (scanner.hasPermission()) startScan(scanner, state) else launcher.launch(permissions)
                        }) {
                            Text(if (state.scanning) "Scanning…" else "Scan for watches")
                        }
                        OutlinedButton(onClick = { if (state.scanning) { scanner.stop(); state.scanning = false } }) {
                            Text("Stop")
                        }
                    }
                    state.message?.let { message ->
                        Text(
                            message,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }

        if (state.devices.isNotEmpty()) {
            item { SectionTitle("Nearby devices", "Tap analyse to score the protocol. Nothing is written to the watch.") }
            items(state.devices, key = { it.address }) { device ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(device.name ?: "Unnamed device", style = MaterialTheme.typography.titleMedium)
                            Text(
                                device.address + "  ·  " + device.rssi + " dBm",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(onClick = {
                            state.profile = identify(device)
                            state.connection = ConnectionState.DISCONNECTED
                        }) {
                            Text("Analyse")
                        }
                    }
                }
            }
        }

        item {
            SectionTitle("Today", "Nothing is shown until a watch has actually synced.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoCard("Steps", if (state.previewMode) "8 421" else "—", "activity", Modifier.weight(1f))
                InfoCard("Heart rate", if (state.previewMode) "71 lpm" else "—", "last reading", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 10.dp)) {
                InfoCard("SpO₂", if (state.previewMode) "97 %" else "—", "sensor value", Modifier.weight(1f))
                InfoCard("Sleep", if (state.previewMode) "7 h 31 m" else "—", "last night", Modifier.weight(1f))
            }
            if (state.previewMode) {
                Text(
                    stringResource(R.string.preview_banner),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        item {
            SectionTitle("Device")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    val profile = state.profile
                    Text(
                        profile?.model ?: "No watch analysed",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        when {
                            profile == null -> "Run a scan and analyse a device to build a fingerprint."
                            profile.supportLevel == SupportLevel.DETECTED -> "Fingerprint captured. Protocol not confirmed."
                            else -> "Level " + profile.supportLevel.level + " · " + profile.supportLevel.label
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val last = state.lastSync
                    val clock = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
                    Text(
                        if (last == null) "Never synced" else "Last sync " + clock.format(last),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = onOpenWatch) { Text("Open watch") }
                        OutlinedButton(onClick = onOpenCreate) { Text("Create face") }
                    }
                }
            }
        }

        item { Column(Modifier.padding(bottom = 20.dp)) {} }
    }
}

private fun startScan(scanner: BleScanner, state: AppState) {
    state.message = null
    val started = scanner.start(
        onDevice = { device ->
            state.devices = (state.devices.filterNot { it.address == device.address } + device)
                .sortedByDescending { it.rssi }
        },
        onError = { error -> state.message = error }
    )
    state.scanning = started
    if (!started) state.message = "Bluetooth is off or unavailable on this phone."
}

private fun identify(device: io.github.smouj.mydevwatch.ble.ScannedDevice): DeviceProfile {
    val fingerprint = DeviceFingerprint(
        name = device.name,
        advertisedServices = device.serviceUuids
    )
    val result = ProtocolRegistry().identify(fingerprint)
    return DeviceProfile(
        id = device.address,
        vendor = null,
        model = device.name ?: device.address,
        protocol = result.protocol,
        capabilities = setOf(Capability.BATTERY),
        statuses = mapOf(Capability.BATTERY to SupportStatus.UNKNOWN),
        supportLevel = SupportLevel.DETECTED,
        confidence = if (result.isConclusive) Confidence.OBSERVED else Confidence.UNKNOWN
    )
}
