package com.example.weather_julia.api

import com.example.weather_julia.data.Weather
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherService {
    @GET("timeline/{cityName}?unitGroup=us&key=MSZ5NUP7BQSCKZK3ZJ5ZSAN3X&contentType=json")
    suspend fun getWeather(
        @Path("cityName") cityName: String
    ): Weather
}