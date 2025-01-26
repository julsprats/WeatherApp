package com.example.weather_julia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather_julia.adapters.WeatherReportAdapter
import com.example.weather_julia.data.WeatherDatabase
import com.example.weather_julia.data.WeatherReportDao
import com.example.weather_julia.databinding.ActivityWeatherHistoryBinding
import kotlinx.coroutines.launch

class WeatherHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherHistoryBinding
    private lateinit var weatherDatabase: WeatherDatabase
    private lateinit var weatherReportDao: WeatherReportDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Room database and DAO
        weatherDatabase = WeatherDatabase.getDatabase(applicationContext)
        weatherReportDao = weatherDatabase.weatherReportDao()

        // Set up RecyclerView
        val adapter = WeatherReportAdapter()
        binding.weatherHistoryRecyclerView.adapter = adapter
        binding.weatherHistoryRecyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve and display saved weather reports
        lifecycleScope.launch {
            val weatherReports = weatherReportDao.getAllWeatherReports()
            adapter.submitList(weatherReports)
        }
    }
}