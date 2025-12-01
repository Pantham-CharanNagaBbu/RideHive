package uk.ac.tees.mad.ridehive.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rides")
data class RideRoom(
    @PrimaryKey val id : Int = 0,
    val userUid: String = "",
    val userName: String = "",
    val from: String = "",
    val destinationName: String = "",
    val destinationLatitude: Double? = null,
    val destinationLongitude: Double? = null,
    val date: String = "",
    val time: String = "",
    val seats: Int = 0,
    val rideId: String = "",
)
