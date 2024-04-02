package com.madhav.weatherapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WeatherDatabaseEntity::class], version = 2)
abstract class WeatherDatabaseClass : RoomDatabase() {
    abstract fun weatherDAO(): WeatherDatabaseAccessObject

    object DatabaseBuilder {
        @Volatile
        private var INSTANCE: WeatherDatabaseClass? = null

        fun getInstance(context: Context): WeatherDatabaseClass {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabaseClass::class.java,
                    "weather_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}