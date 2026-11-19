package com.ingamarin.campusgps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ingamarin.campusgps.ui.PantallaMapa
import com.ingamarin.campusgps.ui.theme.CampusGPSTheme
import com.ingamarin.campusgps.viewmodel.CampusViewModelFactory
import com.ingamarin.campusgps.viewmodel.MapaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusGPSTheme {
                val vm: MapaViewModel = viewModel(factory = CampusViewModelFactory.Factory)
                PantallaMapa(vm)
            }
        }
    }
}
