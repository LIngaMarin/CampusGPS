package com.ingamarin.campusgps.data

import com.ingamarin.campusgps.model.Posicion

/** Contrato para obtener la ubicacion del celular. El ViewModel solo conoce esta interface. */
interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
}
