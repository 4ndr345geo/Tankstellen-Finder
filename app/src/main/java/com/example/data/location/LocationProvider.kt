package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val label: String,
    val isRealGps: Boolean
)

data class CityPreset(
    val name: String,
    val state: String,
    val latitude: Double,
    val longitude: Double
)

class LocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        // Default starting point: Weimar in Thüringen
        val DEFAULT_LOCATION = LocationInfo(
            latitude = 50.9803,
            longitude = 11.3290,
            label = "Weimar in Thüringen",
            isRealGps = false
        )

        val PRESET_CITIES = listOf(
            CityPreset("Weimar", "Thüringen", 50.9803, 11.3290),
            CityPreset("Erfurt", "Thüringen", 50.9850, 11.0290),
            CityPreset("Jena", "Thüringen", 50.9270, 11.5892),
            CityPreset("Berlin", "Berlin", 52.5200, 13.4050),
            CityPreset("Hamburg", "Hamburg", 53.5511, 9.9937),
            CityPreset("München", "Bayern", 48.1351, 11.5820),
            CityPreset("Köln", "Nordrhein-Westfalen", 50.9375, 6.9603),
            CityPreset("Frankfurt a.M.", "Hessen", 50.1109, 8.6821),
            CityPreset("Stuttgart", "Baden-Württemberg", 48.7758, 9.1829),
            CityPreset("Düsseldorf", "Nordrhein-Westfalen", 51.2277, 6.7735),
            CityPreset("Leipzig", "Sachsen", 51.3397, 12.3731),
            CityPreset("Hannover", "Niedersachsen", 52.3759, 9.7320),
            CityPreset("Peine (MTS-K Demo)", "Niedersachsen", 52.3214, 10.2315),
            CityPreset("Nürnberg", "Bayern", 49.4521, 11.0767),
            CityPreset("Dresden", "Sachsen", 51.0504, 13.7373)
        )
    }

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun getDeviceLocation(): LocationInfo? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext null
        }

        try {
            // Try high accuracy current location with timeout
            val cts = CancellationTokenSource()
            val location = suspendCancellableCoroutine<Location?> { continuation ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).addOnSuccessListener { loc ->
                    if (continuation.isActive) continuation.resume(loc)
                }.addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
                continuation.invokeOnCancellation { cts.cancel() }
            }

            val finalLocation = location ?: suspendCancellableCoroutine { continuation ->
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (continuation.isActive) continuation.resume(loc)
                }.addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
            } ?: getLocationFromSystemManager()

            if (finalLocation != null) {
                val resolvedName = resolveAddressName(finalLocation.latitude, finalLocation.longitude)
                return@withContext LocationInfo(
                    latitude = finalLocation.latitude,
                    longitude = finalLocation.longitude,
                    label = resolvedName ?: "Mein GPS-Standort",
                    isRealGps = true
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext null
    }

    @SuppressLint("MissingPermission")
    private fun getLocationFromSystemManager(): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        return try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun resolveAddressName(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    val geocoder = Geocoder(context, Locale.GERMANY)
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        val city = address?.locality ?: address?.subAdminArea ?: address?.adminArea
                        cont.resume(city)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val geocoder = Geocoder(context, Locale.GERMANY)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                val address = addresses?.firstOrNull()
                address?.locality ?: address?.subAdminArea ?: address?.adminArea
            }
        } catch (e: Exception) {
            null
        }
    }
}
