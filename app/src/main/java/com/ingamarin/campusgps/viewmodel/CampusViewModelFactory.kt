package com.ingamarin.campusgps.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ingamarin.campusgps.data.LugaresRepositoryLocal
import com.ingamarin.campusgps.data.UbicacionRepositoryGoogle

/**
 * Crea el MapaViewModel con sus Repository.
 * El Context de la aplicacion se usa aqui para crear el Repository; el ViewModel no lo recibe.
 */
object CampusViewModelFactory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY]!!
            MapaViewModel(
                ubicacionRepo = UbicacionRepositoryGoogle(app),
                lugaresRepo = LugaresRepositoryLocal()
            )
        }
    }
}
