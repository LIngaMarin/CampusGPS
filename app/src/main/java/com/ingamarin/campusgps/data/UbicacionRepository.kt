package com.ingamarin.campusgps.data

import com.ingamarin.campusgps.model.Posicion

/** Contrato para obtener la ubicacion del celular. El ViewModel solo conoce esta interface. */
interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>

    /** true si el dispositivo tiene Google Play Services listo para usar. */
    fun playServicesDisponible(): Boolean
}

/** La ubicacion esta apagada o el GPS no pudo dar una posicion. */
class UbicacionNoDisponibleException :
    Exception("No se pudo obtener la ubicación. Activa el GPS e intenta de nuevo")
