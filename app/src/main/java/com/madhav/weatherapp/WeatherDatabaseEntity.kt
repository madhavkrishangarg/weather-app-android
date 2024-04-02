package com.madhav.weatherapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_data")
data class WeatherDatabaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val city_name: String,
    val max_temp: Double,
    val min_temp: Double,
    val date: String
)
