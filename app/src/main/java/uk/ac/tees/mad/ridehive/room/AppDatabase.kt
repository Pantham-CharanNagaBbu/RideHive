package uk.ac.tees.mad.ridehive.room

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.ac.tees.mad.ridehive.model.Ride

@Database(entities = [RideRoom::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ridesDao(): RidesDao
}