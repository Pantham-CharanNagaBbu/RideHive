package uk.ac.tees.mad.ridehive.model

data class Ride(
    val userUid: String = "",
    val userName: String = "",
    val from: String = "",
    val destinationName: String = "",
    val destinationLatitude: Double? = null,
    val destinationLongitude: Double? = null,
    val date: String = "",
    val time: String = "",
    val seats: Int = 0
)
