package com.ingamarin.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ingamarin.campusgps.data.LugaresRepository
import com.ingamarin.campusgps.data.UbicacionNoDisponibleException
import com.ingamarin.campusgps.data.UbicacionRepository
import com.ingamarin.campusgps.model.Lugar
import com.ingamarin.campusgps.model.LugarConDistancia
import com.ingamarin.campusgps.model.Posicion
import com.ingamarin.campusgps.model.TipoError
import com.ingamarin.campusgps.model.UbicacionUi
import com.ingamarin.campusgps.util.distanciaMetros
import com.ingamarin.campusgps.util.formatearDistancia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla del mapa. Solo conoce las interfaces de los Repository (no Context ni Composables).
 */
class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    private val _lugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) }
    )
    val lugares: StateFlow<List<LugarConDistancia>> = _lugares.asStateFlow()

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    fun buscar() {
        // Sin Play Services no hay ubicacion: se avisa sin intentar
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                "Este dispositivo no tiene Google Play Services",
                TipoError.SIN_PLAY_SERVICES
            )
            return
        }
        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
                .onFailure { _estado.value = errorDe(it) }
        }
    }

    /** Calcula la distancia a cada lugar, ordena de menor a mayor y publica la lista. */
    private fun actualizarDistancias(posicion: Posicion) {
        _lugares.value = lugaresRepo.obtenerLugares()
            .map { LugarConDistancia(it, distanciaMetros(posicion, it)) }
            .sortedBy { it.metros }
        _lugares.value.forEach { Log.d("DISTANCIA", "${it.lugar.nombre}: ${it.metros?.let(::formatearDistancia)}") }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás",
            TipoError.SIN_PERMISO
        )
    }

    /** Lugar elegido en la lista (o con el boton del mas cercano). null = ninguno. */
    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    // ---------- Mejora IA 1: filtro por categoria ----------
    /** Categorias que existen, en el orden de los datos (Estudio, Comida, Eventos, Servicios). */
    val categorias: List<String> = lugaresRepo.obtenerLugares().map { it.categoria }.distinct()

    private val _categoria = MutableStateFlow<String?>(null)   // null = Todas
    val categoria: StateFlow<String?> = _categoria.asStateFlow()

    /**
     * Lo que se ve en el mapa y en la lista. Es aparte de [lugares] para que el filtro
     * no cambie cual es el lugar mas cercano del campus.
     */
    val lugaresVisibles: StateFlow<List<LugarConDistancia>> = combine(lugares, _categoria) { lista, cat ->
        if (cat == null) lista else lista.filter { it.lugar.categoria == cat }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _lugares.value)

    fun filtrarPorCategoria(categoria: String?) {
        _categoria.value = categoria
    }

    // ---------- Mejora IA 2: radio de cercania ----------
    val radiosDisponibles = listOf(100, 200, 500)

    private val _radio = MutableStateFlow(200)
    val radio: StateFlow<Int> = _radio.asStateFlow()

    fun seleccionarRadio(metros: Int) {
        _radio.value = metros
    }

    /** Ids de los lugares que estan dentro del radio. Se recalcula al cambiar el radio o la ubicacion. */
    val idsDentroDelRadio: StateFlow<Set<Int>> = combine(lugares, _radio) { lista, r ->
        lista.filter { item -> item.metros?.let { it <= r } == true }
            .map { it.lugar.id }
            .toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** El primero de la lista ordenada que ya tiene distancia. null mientras no hay ubicacion. */
    val masCercano: StateFlow<Lugar?> = lugares
        .map { lista -> lista.firstOrNull { it.metros != null }?.lugar }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Convierte la excepcion del Repository en un estado de error con su tipo. */
    private fun errorDe(e: Throwable): UbicacionUi.Error = when (e) {
        is SecurityException -> UbicacionUi.Error("Falta el permiso de ubicación", TipoError.SIN_PERMISO)
        is UbicacionNoDisponibleException -> UbicacionUi.Error(
            "No se pudo obtener la ubicación. Activa el GPS e intenta de nuevo",
            TipoError.GPS_APAGADO
        )
        else -> UbicacionUi.Error(e.message ?: "No se pudo obtener la ubicación", TipoError.OTRO)
    }
}
