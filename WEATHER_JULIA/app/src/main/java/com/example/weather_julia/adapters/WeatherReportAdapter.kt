package com.example.weather_julia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather_julia.R
import com.example.weather_julia.data.Weather
import com.example.weather_julia.data.WeatherReport
import com.example.weather_julia.databinding.ItemWeatherReportBinding

class WeatherReportAdapter: ListAdapter<WeatherReport, WeatherReportAdapter.WeatherReportViewHolder>(WeatherReportDiffCallback()) {

    inner class WeatherReportViewHolder(private val binding: ItemWeatherReportBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(weatherReport: WeatherReport) {
            with(binding) {
                cityName.text = weatherReport.cityName
                temperature.text = "Temperature: ${weatherReport.temperature}°C"
                humidity.text = "Humidity: ${weatherReport.humidity}%"
                conditions.text = "Conditions: ${weatherReport.conditions}"
                datetime.text = "Time: ${weatherReport.datetime}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherReportViewHolder {
        val binding = ItemWeatherReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeatherReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherReportViewHolder, position: Int) {
        val weatherReport = getItem(position)
        holder.bind(weatherReport)
    }

    private class WeatherReportDiffCallback : DiffUtil.ItemCallback<WeatherReport>() {
        override fun areItemsTheSame(oldItem: WeatherReport, newItem: WeatherReport): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WeatherReport, newItem: WeatherReport): Boolean {
            return oldItem == newItem
        }
    }

}