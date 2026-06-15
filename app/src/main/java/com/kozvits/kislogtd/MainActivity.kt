package com.kozvits.kislogtd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kozvits.kislogtd.presentation.navigation.AppNavHost
import com.kozvits.kislogtd.presentation.theme.KisloGTDTheme
import com.kozvits.kislogtd.sync.SyncStateRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var syncStateRepository: SyncStateRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by syncStateRepository.settings.collectAsState(initial = null)
            val s = settings
            val sysDark = isConnectedToDarkMode()
            val isDarkTheme = if (s?.themeUserSet == true) s.isDarkTheme else sysDark
            KisloGTDTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun isConnectedToDarkMode(): Boolean {
        return (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_YES) != 0
    }
}
