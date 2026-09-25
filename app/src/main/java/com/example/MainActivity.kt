package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.UE5EditorScreen
import com.example.ui.theme.UE5Theme
import com.example.ui.theme.UE_Background
import com.example.ui.viewmodel.UE5EditorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UE5Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = UE_Background
                ) {
                    val viewModel: UE5EditorViewModel = viewModel()
                    UE5EditorScreen(viewModel = viewModel)
                }
            }
        }
    }
}
