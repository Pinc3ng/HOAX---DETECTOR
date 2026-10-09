package com.hoaxdetector.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hoaxdetector.app.ui.navigation.HoaxDetectorNavGraph
import com.hoaxdetector.app.ui.theme.HoaxDetectorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HoaxDetectorTheme {
                HoaxDetectorNavGraph()
            }
        }
    }
}
