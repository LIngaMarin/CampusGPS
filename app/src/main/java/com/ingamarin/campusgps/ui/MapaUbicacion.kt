package com.ingamarin.campusgps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
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
    masCercanoId: Int?,
    radioMetros: Int,
    modifier: Modifier = Modifier
) {
    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(PUNTO_BASE, 12f)
    }

    // Un estado por lugar (sus posiciones no cambian). Asi se puede abrir su globo al seleccionarlo
    val estadosLugares = remember(lugares.map { it.lugar.id }.sorted()) {
        lugares.associate { it.lugar.id to MarkerState(LatLng(it.lugar.latitud, it.lugar.longitud)) }
    }

    // Cuando llega la ubicacion, la camara viaja hasta ella con zoom 16
    LaunchedEffect(estado) {
        if (estado is UbicacionUi.Lista) {
            val aqui = LatLng(estado.posicion.latitud, estado.posicion.longitud)
            camara.animate(CameraUpdateFactory.newLatLngZoom(aqui, 16f))
        }
    }

    // Al seleccionar un lugar, la camara viaja hasta el con zoom 17 y muestra su nombre
    LaunchedEffect(seleccionado) {
        val estadoLugar = seleccionado?.let { estadosLugares[it] } ?: return@LaunchedEffect
        camara.animate(CameraUpdateFactory.newLatLngZoom(estadoLugar.position, 17f))
        estadoLugar.showInfoWindow()
    }

    GoogleMap(modifier = modifier, cameraPositionState = camara) {
        // Marcador rojo (el de por defecto) solo si ya se conoce la ubicacion
        if (estado is UbicacionUi.Lista) {
            val aqui = LatLng(estado.posicion.latitud, estado.posicion.longitud)
            Marker(
                state = remember(aqui) { MarkerState(position = aqui) },
                title = "Estás aquí"
            )
            // Circulo del radio de cercania alrededor de mi posicion
            Circle(
                center = aqui,
                radius = radioMetros.toDouble(),
                strokeColor = Color(0xFF1E6FB8),
                strokeWidth = 4f,
                fillColor = Color(0x331E6FB8)
            )
        }
        // Un marcador por lugar: azul, y el mas cercano en verde (key = id para que Compose los identifique)
        lugares.forEach { item ->
            key(item.lugar.id) {
                Marker(
                    state = estadosLugares.getValue(item.lugar.id),
                    title = item.lugar.nombre,
                    snippet = item.lugar.categoria,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (item.lugar.id == masCercanoId) BitmapDescriptorFactory.HUE_GREEN
                        else BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }
        }
    }
}
