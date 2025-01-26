package com.example.weather_julia

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.weather_julia.api.RetrofitInstance
import com.example.weather_julia.api.WeatherService
import com.example.weather_julia.data.Weather
import com.example.weather_julia.data.WeatherDatabase
import com.example.weather_julia.data.WeatherReport
import com.example.weather_julia.data.WeatherReportDao
import com.example.weather_julia.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private val TAG:String = "MAIN_ACTIVITY"

    lateinit var binding:ActivityMainBinding

    private lateinit var weatherDatabase: WeatherDatabase
    private lateinit var weatherReportDao: WeatherReportDao

    // Device location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // permissions array
    private val APP_PERMISSIONS_LIST = arrayOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    // showing the permissions dialog box & its result
    private val multiplePermissionsResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {

            resultsList ->
        Log.d(TAG, resultsList.toString())

        var allPermissionsGrantedTracker = true

        for (item in resultsList.entries) {
            if (item.key in APP_PERMISSIONS_LIST && item.value == false) {
                allPermissionsGrantedTracker = false
            }
        }

        if (allPermissionsGrantedTracker == true) {
            var snackbar = Snackbar.make(binding.root, "All permissions granted", Snackbar.LENGTH_LONG)
            snackbar.show()

            // TODO: Get the user's location from the device (GPS, Wifi, etc)
            getDeviceLocation()

        } else {
            var snackbar = Snackbar.make(binding.root, "Some permissions NOT granted", Snackbar.LENGTH_LONG)
            snackbar.show()
            // TODO: Output a rationale for why we need permissions
            // TODO: Disable the get current location button so they can't accidently click on
            //handlePermissionDenied()
        }
    }

    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if not granted
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Log.d(TAG, "Location is null")
                    return@addOnSuccessListener
                }else{
                    binding.cityEditText.setText("Toronto")
                }

                val message = "The device is located at: ${location.latitude}, ${location.longitude}"
                Log.d(TAG, message)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                binding.cityEditText.setText(message)

                try {
                    val geocoder = Geocoder(applicationContext, Locale.getDefault())
                    val searchResults: MutableList<Address>? =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (searchResults.isNullOrEmpty()) {
                        Log.d(TAG, "No matching address found")
                    } else {
                        val matchingAddress: Address = searchResults[0]
                        val output = "${matchingAddress.subThoroughfare} ${matchingAddress.thoroughfare}, ${matchingAddress.locality}, ${matchingAddress.adminArea}, ${matchingAddress.countryName} "
                        binding.cityEditText.setText(output)
                        Log.d(TAG, output)
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Exception: $ex")
                    Snackbar.make(binding.root, "Error getting location information", Snackbar.LENGTH_LONG)
                        .show()
                }
            }
    }

    private fun checkLocationPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set menu
        setSupportActionBar(this.binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // fusedLocationProvider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check and request location permissions if needed
        if (checkLocationPermissions()) {
            // Permissions are granted --> get location
            var snackbar = Snackbar.make(binding.root, "Permissions granted", Snackbar.LENGTH_LONG)
            snackbar.show()
            getDeviceLocation()
        } else {
            // Request location permissions
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
        }

        binding.getWeatherButton.setOnClickListener {
            val geocoder = Geocoder(applicationContext, Locale.getDefault())
            val addressFromUI = binding.cityEditText.text.toString()
            Log.d(TAG, "Getting ${addressFromUI}")
            try {
                val searchResults: MutableList<Address>? =
                    geocoder.getFromLocationName(addressFromUI, 1)
                if (searchResults == null) {
                    Log.e(TAG, "searchResults is null!")
                    return@setOnClickListener
                }
                // if not null...
                if (searchResults.size == 0) {
                    var snackbar = Snackbar.make(binding.root, "Search results are empty.", Snackbar.LENGTH_LONG)
                    snackbar.show()
                } else {
                    val foundLocation: Address = searchResults.get(0)
                    var message = "Location: ${foundLocation.countryName}"
                    var snackbar = Snackbar.make(binding.root, "Country name: ${foundLocation.countryName}", Snackbar.LENGTH_LONG)
                    snackbar.show()
                    Log.d(TAG, message)
                    var api: WeatherService = RetrofitInstance.retrofitService
                    lifecycleScope.launch {
                        val wth: Weather = api.getWeather(addressFromUI)
                        Log.d(TAG, wth.toString())
                        val currTempFahrenheit = wth.currentConditions.temp
                        val currTempCelsius = (currTempFahrenheit - 32) * 5 / 9
                        val formattedCurrTemp = String.format("%.2f", currTempCelsius)

                        val currTemp = "Current Temperature: $formattedCurrTemp°C"
                        binding.Temperature.setText(currTemp)

                        val humidity = "Humidity: ${wth.currentConditions.humidity}"
                        binding.Humidity.setText(humidity)

                        val condition = "Condition: ${wth.currentConditions.conditions}"
                        binding.WeatherConditions.setText(condition)

                        val datetime = "Time: ${wth.currentConditions.datetime}"
                        binding.Time.setText(datetime)
                    }
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Exception: $ex")
            }
        }

        binding.cityEditText.setOnClickListener {
            // Check for permissions and do actions
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
        }

        // Initialize Room database and DAO
        weatherDatabase = WeatherDatabase.getDatabase(applicationContext)
        weatherReportDao = weatherDatabase.weatherReportDao()

        binding.saveWeatherButton.setOnClickListener {
            // Retrieve information from the API response
            val cityName = binding.cityEditText.text.toString()
            val addressFromUI = binding.cityEditText.text.toString()
            Log.d(TAG, "Saving $addressFromUI in Room database")

            lifecycleScope.launch {
                try {
                    var api: WeatherService = RetrofitInstance.retrofitService
                    val wth: Weather = api.getWeather(addressFromUI)

                    val temperature = wth.currentConditions.temp
                    val humidity = wth.currentConditions.humidity
                    val conditions = wth.currentConditions.conditions
                    val datetime = wth.currentConditions.datetime

                    // Save the info to the Room db
                    saveWeatherToDatabase(cityName, temperature, humidity, conditions, datetime)
                } catch (ex: Exception) {
                    Log.e(TAG, "Exception: $ex")
                }
            }
        }
    }

    // DATABASE
    private fun saveWeatherToDatabase(
        cityName: String,
        temperature: Double,
        humidity: Double,
        conditions: String,
        datetime: String
    ) {
        val weatherReport = WeatherReport(
            cityName = cityName,
            temperature = temperature,
            humidity = humidity,
            conditions = conditions,
            datetime = datetime
        )

        lifecycleScope.launch {
            // Insert the weather report into the Room db
            weatherReportDao.insert(weatherReport)

            Snackbar.make(binding.root, "Weather report saved", Snackbar.LENGTH_LONG).show()
        }
    }

    // MENU
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu_items, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_history -> {
                val intent = Intent(this, WeatherHistoryActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}