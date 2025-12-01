package uk.ac.tees.mad.ridehive.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import uk.ac.tees.mad.ridehive.model.Ride

@Dao
interface RidesDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: List<RideRoom>)

    @Query("SELECT * FROM rides")
    suspend fun getAllRides(): List<RideRoom>

    @Query("DELETE FROM rides")
    suspend fun deleteAllRides()


}