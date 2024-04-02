package com.madhav.weatherapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WeatherDatabaseAccessObject {
    @Insert
    fun upsert(weatherData: WeatherDatabaseEntity)

    @Query("SELECT * FROM weather_data WHERE city_name = :cityName AND date = :date")
    fun getWeatherData(cityName: String, date: String): WeatherDatabaseEntity?

}