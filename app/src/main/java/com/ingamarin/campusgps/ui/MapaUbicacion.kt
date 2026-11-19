package com.ingamarin.campusgps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.ingamarin.campusgps.model.LugarConDistancia
import com.ingamarin.campusgps.model.UbicacionUi

/** Punto base del campus (Lima). La camara empieza aqui. */
private val PUNTO_BASE = LatLng(-12.0464, -77.0428)

/**
 * Mapa de Google. Solo dibuja lo que recibe: no consulta Repository ni calcula nada.
 */
@Composable
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    modifier: Modifier = Modifier
) {
    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(PUNTO_BASE, 12f)
    }

    // Cuando llega la ubicacion, la camara viaja hasta ella con zoom 16
    LaunchedEffect(estado) {
        if (estado is UbicacionUi.Lista) {
            val aqui = LatLng(estado.posicion.latitud, estado.posicion.longitud)
            camara.animate(CameraUpdateFactory.newLatLngZoom(aqui, 16f))
        }
    }

    GoogleMap(modifier = modifier, cameraPositionState = camara) {
        // Marcador rojo (el de por defecto) solo si ya se conoce la ubicacion
        if (estado is UbicacionUi.Lista) {
            val aqui = LatLng(estado.posicion.latitud, estado.posicion.longitud)
            Marker(
                state = remember(aqui) { MarkerState(position = aqui) },
                title = "Estás aquí"
            )
        }
        // Un marcador azul por cada lugar del campus (key = id para que Compose los identifique)
        lugares.forEach { item ->
            key(item.lugar.id) {
                val posicion = LatLng(item.lugar.latitud, item.lugar.longitud)
                Marker(
                    state = remember(posicion) { MarkerState(position = posicion) },
                    title = item.lugar.nombre,
                    snippet = item.lugar.categoria,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }
        }
    }
}
