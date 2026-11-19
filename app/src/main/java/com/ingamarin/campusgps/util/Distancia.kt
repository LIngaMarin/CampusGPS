package com.ingamarin.campusgps.util

import android.location.Location
import com.ingamarin.campusgps.model.Lugar
import com.ingamarin.campusgps.model.Posicion

/**
 * Distancia en metros desde la posicion del usuario hasta un lugar.
 * Location.distanceBetween guarda el resultado en resultado[0]. Orden: (lat1, lon1, lat2, lon2).
 */
fun distanciaMetros(desde: Posicion, hasta: Lugar): Float {
    val resultado = FloatArray(1)
    Location.distanceBetween(
        desde.latitud, desde.longitud,
        hasta.latitud, hasta.longitud,
        resultado
    )
    return resultado[0]
}
