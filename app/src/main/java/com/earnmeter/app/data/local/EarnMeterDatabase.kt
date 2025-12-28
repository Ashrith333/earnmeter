package com.earnmeter.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.earnmeter.app.data.local.dao.RideDao
import com.earnmeter.app.data.local.dao.UserSettingsDao
import com.earnmeter.app.data.local.entity.RideEntity
import com.earnmeter.app.data.local.entity.UserSettingsEntity

@Database(
    entities = [
        RideEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class EarnMeterDatabase : RoomDatabase() {
    
    abstract fun rideDao(): RideDao
    abstract fun userSettingsDao(): UserSettingsDao
    
    companion object {
        private const val DATABASE_NAME = "earn_meter_db"
        
        @Volatile
        private var INSTANCE: EarnMeterDatabase? = null
        
        fun getInstance(context: Context): EarnMeterDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): EarnMeterDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                EarnMeterDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

