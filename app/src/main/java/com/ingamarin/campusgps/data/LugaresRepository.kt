package com.ingamarin.campusgps.data

import com.ingamarin.campusgps.model.Lugar

/** Contrato para obtener los lugares del campus. */
interface LugaresRepository {
    fun obtenerLugares(): List<Lugar>
}
