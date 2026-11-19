package com.ingamarin.campusgps.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ingamarin.campusgps.model.LugarConDistancia
import com.ingamarin.campusgps.util.formatearDistancia

/** Colores del punto de la tarjeta: los mismos de los marcadores del mapa. */
private val AzulMarcador = Color(0xFF2E8BE6)
private val VerdeMarcador = Color(0xFF2EAA4F)

/**
 * Lista de lugares. No conoce el ViewModel: recibe los datos y avisa el toque con onClick.
 */
@Composable
fun ListaLugares(
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    masCercanoId: Int?,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val estadoLista = rememberLazyListState()

    // Si cambia el primero (nuevo orden), la lista vuelve arriba para que se vea el mas cercano
    LaunchedEffect(lugares.firstOrNull()?.lugar?.id) {
        estadoLista.scrollToItem(0)
    }

    LazyColumn(
        state = estadoLista,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(lugares, key = { it.lugar.id }) { item ->
            TarjetaLugar(
                item = item,
                seleccionada = item.lugar.id == seleccionado,
                esMasCercano = item.lugar.id == masCercanoId,
                onClick = { onClick(item.lugar.id) }
            )
        }
    }
}

@Composable
private fun TarjetaLugar(
    item: LugarConDistancia,
    seleccionada: Boolean,
    esMasCercano: Boolean,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionada) colores.primaryContainer else colores.surfaceVariant
        ),
        border = if (seleccionada) BorderStroke(2.dp, colores.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(if (esMasCercano) VerdeMarcador else AzulMarcador, CircleShape)
            )
            Box(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.lugar.nombre, fontWeight = FontWeight.Bold)
                Text(
                    item.lugar.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = colores.onSurfaceVariant
                )
            }
            Text(
                text = item.metros?.let { formatearDistancia(it) } ?: "Sin calcular",
                fontWeight = FontWeight.Bold,
                color = colores.primary
            )
        }
    }
}
