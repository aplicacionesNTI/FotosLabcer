package com.itevebasa.fotoslabcer.auxiliar

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Localizacion {

    //Devuelve la localización guardada en el dispositivo
    companion object {
        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
        suspend fun getCurrentLocation(context: Context): Location? {
            return suspendCoroutine { continuation ->
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).apply {
                        setWaitForAccurateLocation(false) // No esperar precisión extrema si no es necesario
                        setMinUpdateIntervalMillis(1000) // Intervalo mínimo entre actualizaciones
                        setMaxUpdates(1) // Solo obtener una ubicación
                    }.build()

                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            val newLocation = locationResult.lastLocation
                            if (newLocation != null) {
                                Log.d("Location", "Ubicación obtenida: $newLocation")
                                continuation.resume(newLocation)
                            } else {
                                Log.e("Location", "No se pudo obtener la ubicación")
                                continuation.resume(null)
                            }
                            fusedLocationClient.removeLocationUpdates(this) // Detener actualizaciones
                        }
                    }
                    // Solicitar ubicación en tiempo real
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                }.addOnFailureListener {
                    Log.e("Location", "Error obteniendo ubicación", it)
                    continuation.resume(null)
                }
            }
        }
    }
}