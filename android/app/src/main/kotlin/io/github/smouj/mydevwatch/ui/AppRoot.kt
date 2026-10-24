package io.github.smouj.mydevwatch.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import io.github.smouj.mydevwatch.R

enum class Tab(val labelRes: Int, val icon: ImageVector) {
    HOME(R.string.tab_home, Icons.Filled.Home),
    HEALTH(R.string.tab_health, Icons.Filled.Favorite),
    ACTIVITY(R.string.tab_activity, Icons.Filled.Star),
    WATCH(R.string.tab_watch, Icons.Filled.Build),
    CREATE(R.string.tab_create, Icons.Filled.Create)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDevWatchApp() {
    val state = remember { AppState() }
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var settings by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (settings) stringResource(R.string.settings) else "My Dev Watch")
                },
                navigationIcon = {
                    if (settings) {
                        IconButton(onClick = { settings = false }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (!settings) {
                        IconButton(onClick = { settings = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        bottomBar = {
            if (!settings) {
                NavigationBar {
                    Tab.entries.forEach { entry ->
                        NavigationBarItem(
                            selected = tab == entry,
                            onClick = { tab = entry },
                            icon = { Icon(entry.icon, contentDescription = null) },
                            label = { Text(stringResource(entry.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        when {
            settings -> SettingsScreen(
                state = state,
                modifier = Modifier.padding(padding)
            )
            tab == Tab.HOME -> HomeScreen(
                state = state,
                onOpenWatch = { tab = Tab.WATCH },
                onOpenCreate = { tab = Tab.CREATE },
                modifier = Modifier.padding(padding)
            )
            tab == Tab.HEALTH -> HealthScreen(state, Modifier.padding(padding))
            tab == Tab.ACTIVITY -> ActivityScreen(state, Modifier.padding(padding))
            tab == Tab.WATCH -> WatchScreen(state, Modifier.padding(padding))
            tab == Tab.CREATE -> CreateScreen(state, Modifier.padding(padding))
        }
    }
}
