package io.github.smouj.mydevwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.smouj.mydevwatch.ui.MyDevWatchApp
import io.github.smouj.mydevwatch.ui.theme.MyDevWatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyDevWatchTheme {
                MyDevWatchApp()
            }
        }
    }
}
