package com.enote.planning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.enote.planning.ui.TaskScreen
import com.enote.planning.ui.theme.PlanningTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideNavigationUI()
        
        // Use a non-deprecated way if possible, or suppress if it's the only way for this target
        @Suppress("DEPRECATION")
        window.navigationBarColor = Color.Transparent.toArgb()

        val sharedPref = getPreferences(MODE_PRIVATE)
        
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            // Load saved theme or use system default
            var isDarkMode by remember { 
                mutableStateOf(sharedPref.getBoolean("is_dark_mode", systemDark)) 
            }

            PlanningTheme(darkTheme = isDarkMode) {
                TaskScreen(
                    isDarkMode = isDarkMode,
                    onThemeToggle = { 
                        isDarkMode = !isDarkMode
                        // Save preference using KTX extension for cleaner code
                        sharedPref.edit {
                            putBoolean("is_dark_mode", isDarkMode)
                        }
                    }
                )
            }
        }
    }

    private fun hideNavigationUI() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Hide only navigation bars (bottom), keep status bars (top) visible
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideNavigationUI()
        }
    }
}
