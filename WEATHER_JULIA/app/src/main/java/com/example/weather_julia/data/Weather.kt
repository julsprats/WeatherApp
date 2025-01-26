package com.example.weather_julia.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json


data class Weather(
    @Json(name = "queryCost") val queryCost: Long,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "resolvedAddress") val resolvedAddress: String,
    @Json(name = "address") val address: String,
    @Json(name = "timezone") val timezone: String,
    @Json(name = "tzoffset") val tzoffset: Long,
    @Json(name = "currentConditions") val currentConditions: CurrentConditions,
)
