package uk.ac.tees.mad.ridehive.utilities
import android.location.Location

fun calculateDistance(
    fromLat: Double,
    fromLng: Double,
    toLat: Double,
    toLng: Double
): Float {
    val results = FloatArray(1)
    Location.distanceBetween(fromLat, fromLng, toLat, toLng, results)
    return results[0] / 1000f // return in KM
}

