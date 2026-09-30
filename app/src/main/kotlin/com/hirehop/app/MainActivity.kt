package com.hirehop.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hirehop.app.ui.HhApp
import com.hirehop.app.ui.rememberHhAppState
import com.hirehop.core.designsystem.theme.HhTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            HhTheme {
                HhApp(appState = rememberHhAppState())
            }
        }
    }
}
