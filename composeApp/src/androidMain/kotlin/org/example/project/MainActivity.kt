package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        println("ach ich glaub ich weiß jetz wies geht!")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        CONTEXT = this.applicationContext
        setContent {
            App(JsonRepository(AndroidFileHandler(this)))
        }
    }
}
