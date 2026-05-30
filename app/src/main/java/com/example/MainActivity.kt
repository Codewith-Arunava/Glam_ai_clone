package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.EditorViewModel
import com.example.ui.screens.CreativeEditor
import com.example.ui.screens.StudioHub
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
    Hub, Editor
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: EditorViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(Screen.Hub) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(
                        targetState = currentScreen,
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            Screen.Hub -> {
                                StudioHub(
                                    viewModel = viewModel,
                                    onNavigateToEditor = { currentScreen = Screen.Editor }
                                )
                            }
                            Screen.Editor -> {
                                CreativeEditor(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.Hub }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
