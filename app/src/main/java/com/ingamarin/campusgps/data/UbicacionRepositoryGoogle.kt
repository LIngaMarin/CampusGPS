package com.ingamarin.campusgps.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
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

    private val appContext = context.applicationContext
    private val cliente = LocationServices.getFusedLocationProviderClient(appContext)
    private val locationManager = appContext.getSystemService(LocationManager::class.java)

    override fun playServicesDisponible(): Boolean =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(appContext) == ConnectionResult.SUCCESS

    // El permiso se revisa en la pantalla antes de llamar; si falta, Android lanza SecurityException
    @SuppressLint("MissingPermission")
    override suspend fun obtenerPosicion(): Result<Posicion> = try {
        // Ubicacion apagada en el celular: ni lo intentamos
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) throw UbicacionNoDisponibleException()

        val ubicacion = cliente.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).await()
        // getCurrentLocation devuelve null si no puede determinar la ubicacion
        if (ubicacion == null) {
            Result.failure(UbicacionNoDisponibleException())
        } else {
            Result.success(Posicion(ubicacion.latitude, ubicacion.longitude))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
