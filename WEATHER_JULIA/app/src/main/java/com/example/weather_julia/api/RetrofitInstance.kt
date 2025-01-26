package com.example.weather_julia.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {
    // Put BASE_URL here
    private const val BASE_URL: String = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/"

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor(
            HttpLoggingInterceptor.Logger { message ->
                println("LOG-APP: $message")
            }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    // used to ensure Moshi annotations work with Kotlin
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .client(httpClient)
        .build()

    private val dynamicUrlInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val modifiedUrl = originalRequest.url.newBuilder()
            .addQueryParameter("latitude", "your_latitude_value")
            .addQueryParameter("longitude", "your_longitude_value")
            .build()

        val modifiedRequest = originalRequest.newBuilder()
            .url(modifiedUrl)
            .build()

        chain.proceed(modifiedRequest)
    }

    private val dynamicUrlClient = httpClient.newBuilder()
        .addInterceptor(dynamicUrlInterceptor)
        .build()

    // update this to return an instance of the Retrofit instance associated
    // with your base URL and dynamic URL capabilities
    val retrofitService: WeatherService by lazy {
        retrofit.newBuilder()
            .client(dynamicUrlClient)
            .build()
            .create(WeatherService::class.java)
    }
}