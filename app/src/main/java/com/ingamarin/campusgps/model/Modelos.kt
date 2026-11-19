package com.ingamarin.campusgps.model

/** Una coordenada geografica (la del usuario). */
data class Posicion(val latitud: Double, val longitud: Double)

/** Un punto del campus (datos ficticios). */
data class Lugar(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val latitud: Double,
    val longitud: Double
)

// metros = null mientras todavia no se conoce la ubicacion del usuario
data class LugarConDistancia(val lugar: Lugar, val metros: Float?)

/** Que salio mal: decide el mensaje y la accion (Abrir ajustes, Reintentar o ninguna). */
enum class TipoError { SIN_PERMISO, GPS_APAGADO, SIN_PLAY_SERVICES, OTRO }

/** Estados de la busqueda de ubicacion. La pantalla dibuja segun el estado. */
sealed interface UbicacionUi {
    data object Inicial : UbicacionUi
    data object Buscando : UbicacionUi
    data class Lista(val posicion: Posicion) : UbicacionUi
    data class Error(val mensaje: String, val tipo: TipoError) : UbicacionUi
}
