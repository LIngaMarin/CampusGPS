package com.ingamarin.campusgps.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ingamarin.campusgps.model.UbicacionUi
import com.ingamarin.campusgps.util.formatearDistancia
import com.ingamarin.campusgps.viewmodel.MapaViewModel

/** Pantalla principal. Solo dibuja segun el estado del ViewModel y le avisa los eventos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val lugares by vm.lugares.collectAsStateWithLifecycle()
    val lugaresVisibles by vm.lugaresVisibles.collectAsStateWithLifecycle()
    val categoria by vm.categoria.collectAsStateWithLifecycle()
    val radio by vm.radio.collectAsStateWithLifecycle()
    val idsDentroDelRadio by vm.idsDentroDelRadio.collectAsStateWithLifecycle()
    val seleccionado by vm.seleccionado.collectAsStateWithLifecycle()
    val masCercano by vm.masCercano.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Pide FINE y COARSE a la vez. Android dibuja el cuadro; aqui solo recibimos la respuesta
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        // Basta con que acepte uno (precisa o aproximada)
        if (resultado.values.any { it }) vm.buscar() else vm.permisoDenegado()
    }

    // Abre la pantalla de ajustes de ESTA app (para dar el permiso a mano)
    fun abrirAjustes() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", contexto.packageName, null)
        )
        contexto.startActivity(intent)
    }

    fun alPulsar() {
        val tienePermiso = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).any { ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED }

        if (tienePermiso) {
            vm.buscar()   // ya lo tiene: no vuelve a preguntar
        } else {
            lanzador.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CampusGPS") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chips de categoria (Todas + una por categoria)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = categoria == null,
                    onClick = { vm.filtrarPorCategoria(null) },
                    label = { Text("Todas") }
                )
                vm.categorias.forEach { c ->
                    FilterChip(
                        selected = categoria == c,
                        onClick = { vm.filtrarPorCategoria(c) },
                        label = { Text(c) }
                    )
                }
            }

            // El mapa ocupa todo el espacio libre de arriba
            MapaUbicacion(
                estado = estado,
                lugares = lugaresVisibles,
                seleccionado = seleccionado,
                masCercanoId = masCercano?.id,
                radioMetros = radio,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Selector del radio de cercania
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Radio:", style = MaterialTheme.typography.bodyMedium)
                    vm.radiosDisponibles.forEach { r ->
                        FilterChip(
                            selected = radio == r,
                            onClick = { vm.seleccionarRadio(r) },
                            label = { Text("$r m") }
                        )
                    }
                }

                Button(
                    onClick = { alPulsar() },
                    enabled = estado !is UbicacionUi.Buscando
                ) {
                    Text("Obtener mi ubicación")
                }

                // Deshabilitado hasta que se conozca el mas cercano
                OutlinedButton(
                    onClick = { masCercano?.let { vm.seleccionar(it.id) } },
                    enabled = masCercano != null
                ) {
                    Text("Ir al más cercano")
                }

                masCercano?.let { lugar ->
                    val distancia = lugares.firstOrNull { it.lugar.id == lugar.id }?.metros
                    Text(
                        "Más cercano: ${lugar.nombre}" + (distancia?.let { " (${formatearDistancia(it)})" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                when (val e = estado) {
                    UbicacionUi.Inicial -> Text("Pulsa el botón para ver dónde estás")
                    UbicacionUi.Buscando -> CircularProgressIndicator()
                    is UbicacionUi.Lista -> Text(
                        "Lat: %.5f · Lng: %.5f".format(e.posicion.latitud, e.posicion.longitud),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    is UbicacionUi.Error -> MensajeError(
                        error = e,
                        onAbrirAjustes = { abrirAjustes() },
                        onReintentar = { alPulsar() }
                    )
                }
            }

            // Lista de lugares con altura fija debajo del mapa
            ListaLugares(
                lugares = lugaresVisibles,
                seleccionado = seleccionado,
                masCercanoId = masCercano?.id,
                dentroDelRadio = idsDentroDelRadio,
                onClick = { vm.seleccionar(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
    }
}
