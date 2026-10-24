package io.github.smouj.mydevwatch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.smouj.mydevwatch.model.Capability
import io.github.smouj.mydevwatch.model.ProtocolId
import io.github.smouj.mydevwatch.model.SupportStatus
import io.github.smouj.mydevwatch.ui.theme.Palette

@Composable
fun HealthScreen(state: AppState, modifier: Modifier = Modifier) {
    val profile = state.profile
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        SectionTitle("Health", "Only values the watch actually reports are shown.")
        if (profile == null) {
            EmptyState(
                "No watch connected",
                "Health data becomes available after a device is analysed and its protocol is confirmed."
            )
        } else {
            Capability.entries.filter { it.category == Capability.Category.HEALTH }.forEach { capability ->
                StatusLine(capability.label, profile.statusOf(capability))
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "Measurements come from the watch sensor. They are not a medical device and must not be used for diagnosis.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                if (state.previewMode) "Preview: 71 lpm · 97 % SpO₂ · 7 h 31 m sleep" else "No measurements synced yet.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ActivityScreen(state: AppState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        SectionTitle("Activity", "Steps, distance and workouts."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoCard("Steps", if (state.previewMode) "8 421" else "—", "today", Modifier.weight(1f))
            InfoCard("Distance", if (state.previewMode) "6,2 km" else "—", "today", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 10.dp)) {
            InfoCard("Calories", if (state.previewMode) "412" else "—", "active", Modifier.weight(1f))
            InfoCard("Active", if (state.previewMode) "48 min" else "—", "moving", Modifier.weight(1f))
        }
        SectionTitle("History", "A failed sleep sync never hides your steps.")
        EmptyState(
            "No history yet",
            "Activity history appears here after the first successful sync. Preview mode is a layout aid, not data."
        )
    }
}

@Composable
fun WatchScreen(state: AppState, modifier: Modifier = Modifier) {
    val profile = state.profile
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        SectionTitle("Watch")
        if (profile == null) {
            EmptyState(
                "No watch analysed",
                "Scan from the Home tab, then analyse a device. My Dev Watch never assumes a retail name equals a firmware."
            )
            return@Column
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(profile.model, style = MaterialTheme.typography.titleLarge)
                Text(
                    "Protocol: " + protocolLabel(profile.protocol),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Profile confidence: " + profile.confidence.name.lowercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Compatibility level " + profile.supportLevel.level + " · " + profile.supportLevel.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.Teal
                )
            }
        }
        SectionTitle("Capabilities", "Unverified is not the same as unavailable.")
        Capability.entries.forEach { capability ->
            StatusLine(capability.label, profile.statusOf(capability))
        }
        SectionTitle("Laboratory")
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("BLE lab", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Raw GATT inspection, hex console and capture export. Planned, not wired to the radio yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.padding(top = 10.dp)) {
                    Text("Open lab")
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Firmware", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Read-only analysis exists in the core library and the CLI. Flashing is deliberately absent.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CreateScreen(state: AppState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        SectionTitle("Create", "Watch-face projects, local and exportable.")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .aspectRatio(1f)
                        .background(Palette.Graphite, RoundedCornerShape(26.dp))
                        .border(2.dp, Palette.Teal, RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("08:24", style = MaterialTheme.typography.displaySmall, color = Palette.OffWhite)
                        Text("8 421 steps", style = MaterialTheme.typography.bodyMedium, color = Palette.Teal)
                        Text("● 86 %", style = MaterialTheme.typography.bodyMedium, color = Palette.Muted)
                    }
                }
                Text(
                    "240 × 240 · RGB565 · circular preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
        SectionTitle("Layers", "Paint order, visibility and lock state.")
        listOf("Time", "Steps", "Battery", "Background").forEach { layer ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(layer, style = MaterialTheme.typography.bodyLarge)
                Text("editable", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(
            "The editor canvas is real. Compilation to a vendor .bin needs a verified device profile, so export is disabled until one exists.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(modifier = Modifier.padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {}, enabled = false) { Text("Export .bin") }
            OutlinedButton(onClick = {}) { Text("Duplicate") }
        }
    }
}

@Composable
fun SettingsScreen(state: AppState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        SectionTitle("Privacy", "Local-first is enforced, not promised.")
        SettingRow("Preview data", "Show labelled sample values in the dashboard.", state.previewMode) {
            state.previewMode = it
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("No account, no analytics, no advertising SDK. Health data stays in the phone database.", style = MaterialTheme.typography.bodyMedium)
        SectionTitle("Data")
        Text(
            "Export and deletion are part of the storage milestone; the buttons below are disabled rather than pretending to work.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = {}, enabled = false) { Text("Export data") }
            OutlinedButton(onClick = {}, enabled = false) { Text("Delete all") }
        }
        SectionTitle("About")
        Text("My Dev Watch 0.1.0", style = MaterialTheme.typography.titleMedium)
        Text("Apache-2.0 · github.com/smouj/my-dev-watch", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "Protocol facts carry an evidence level. Nothing becomes Stable without a recorded field test.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

private fun protocolLabel(id: ProtocolId): String = id.displayName
