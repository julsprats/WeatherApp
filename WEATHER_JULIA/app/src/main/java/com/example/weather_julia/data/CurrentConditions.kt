package com.example.weather_julia.data

data class CurrentConditions(
    val datetime: String,
    val temp: Double,
    val humidity: Double,
    val conditions: String,
)