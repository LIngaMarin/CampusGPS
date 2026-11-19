package com.ingamarin.campusgps.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.ingamarin.campusgps.model.Posicion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Obtiene la ubicacion con FusedLocationProviderClient (Google Play Services).
 * El Context solo vive aqui: el ViewModel no lo conoce.
 */
class UbicacionRepositoryGoogle(context: Context) : UbicacionRepository {

    private val cliente = LocationServices.getFusedLocationProviderClient(context)

    // El permiso se revisa en la pantalla antes de llamar; si falta, Android lanza SecurityException
    @SuppressLint("MissingPermission")
    override suspend fun obtenerPosicion(): Result<Posicion> = try {
        val ubicacion = cliente.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).await()
        if (ubicacion == null) {
            Result.failure(IllegalStateException("No se pudo obtener la ubicación"))
        } else {
            Result.success(Posicion(ubicacion.latitude, ubicacion.longitude))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
