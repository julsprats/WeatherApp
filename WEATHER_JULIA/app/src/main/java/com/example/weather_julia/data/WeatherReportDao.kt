package com.example.weather_julia.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weatherReport: WeatherReport)

    @Query("SELECT * FROM weather_reports")
    suspend fun getAllWeatherReports(): List<WeatherReport>
}