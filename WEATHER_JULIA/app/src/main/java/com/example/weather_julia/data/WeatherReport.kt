package com.example.weather_julia.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_reports")
data class WeatherReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityName: String,
    val temperature: Double,
    val humidity: Double,
    val conditions: String,
    val datetime: String
)