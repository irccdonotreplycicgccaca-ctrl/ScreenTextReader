package com.krmob.screentextreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.krmob.screentextreader.core.designsystem.theme.ScreenTextReaderTheme
import com.krmob.screentextreader.feature.capture.ui.CaptureRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTextReaderTheme {
                CaptureRoute()
            }
        }
    }
}
