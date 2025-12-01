package uk.ac.tees.mad.ridehive

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uk.ac.tees.mad.ridehive.room.AppDatabase
import uk.ac.tees.mad.ridehive.room.RidesDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun providesFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideRidesDao(db: AppDatabase): RidesDao {
        return db.ridesDao()
    }

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context) : AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rides_database"
        ).build()
    }
}