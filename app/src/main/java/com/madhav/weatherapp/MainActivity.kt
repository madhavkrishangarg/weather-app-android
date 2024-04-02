package com.madhav.weatherapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.DropdownList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {
    lateinit var WeatherDatabaseAccessObject: WeatherDatabaseAccessObject
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            getWeatherData(37.7749, -122.4194, "2022-01-01", "2022-01-02")
            WeatherApp()
        }
    }
}


fun getWeatherData(
    latitude: Double, longitude: Double, start_date: String, end_date: String
) {
    val api = Retrofit.Builder().baseUrl("https://archive-api.open-meteo.com/v1/")
        .addConverterFactory(GsonConverterFactory.create()).build().create(WeatherAPI::class.java)
    api.getWeather(latitude, longitude, start_date, end_date, "temperature_2m")
        .enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>, response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.i("WeatherResponse", it.toString())
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.i("WeatherResponse", t.message.toString())
            }

        })
}

suspend fun returnWeatherData(
    latitude: Double, longitude: Double, start_date: String, end_date: String
): WeatherResponse {
    val api = Retrofit.Builder().baseUrl("https://archive-api.open-meteo.com/v1/")
        .addConverterFactory(GsonConverterFactory.create()).build().create(WeatherAPI::class.java)
    Log.i("WeatherResponse", "Getting weather information")
    return api.returnWeather(latitude, longitude, start_date, end_date, "temperature_2m").body()!!
}

@Preview(device = "id:pixel_4a")
@Composable
fun WeatherApp() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //update the date to get the weather data for a different date
        DatePickerButton()
    }
}

@Composable
fun DatePickerButton() {
    val weatherDatabaseClass =
        WeatherDatabaseClass.DatabaseBuilder.getInstance(LocalContext.current)
    val weatherDatabaseAccessObject = weatherDatabaseClass.weatherDAO()
    val context = LocalContext.current
    val selectedDate = remember { mutableStateOf("") }
    var selectedDateCalendar = remember { mutableStateOf(Calendar.getInstance()) }
    Button(
        onClick = {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context, { _, year, month, day ->
                    //check that the selected date is after 1940
                    if (year >= 1940 && year < 2026) {
                        // Format the date as yyyy-MM-dd
                        if (month < 9) {
                            if (day < 10) {
                                selectedDate.value = "$year-0${month + 1}-0$day"
                            } else {
                                selectedDate.value = "$year-0${month + 1}-$day"
                            }
                        } else {
                            if (day < 10) {
                                selectedDate.value = "$year-${month + 1}-0$day"
                            } else {
                                selectedDate.value = "$year-${month + 1}-$day"
                            }
                        }
                        selectedDateCalendar.value = Calendar.getInstance()
                        selectedDateCalendar.value.set(year, month, day)
                        Toast.makeText(context, selectedDate.value, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Please select a date after 1940 and before 2026",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, currentYear, currentMonth, currentDay
            )
            datePickerDialog.datePicker.minDate = getMinDateInMillis()
            datePickerDialog.show()
        },
        modifier = Modifier
            .padding(16.dp)
            .width(172.dp)
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
    ) {
        Text(
            text = "Select Date", fontSize = 20.sp, textAlign = TextAlign.Center
        )
    }
    Text(
        text = "Selected Date: ${selectedDate.value}",
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(36.dp))
    val cityList = listOf(
        City("New Delhi", 28.6139, 77.2090),
        City("London", 51.5074, -0.1278),
        City("New York", 40.7128, -74.0060),
    )

    var minTemp by remember { mutableStateOf(0.0) };
    var maxTemp by remember { mutableStateOf(0.0) };
    var weatherResponse = remember { mutableStateOf<WeatherResponse?>(null) }
    Column(
        modifier = Modifier.background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        cityList.forEach { city ->
            Button(
                onClick = {
                    if (selectedDateCalendar.value.after(Calendar.getInstance())) {
                        weatherResponse.value = null
                        val selectedDateCalendar_copy =
                            selectedDateCalendar.value.clone() as Calendar
//                        Log.i(selectedDate.value, "Selected Date")
                        GlobalScope.launch {
                            try {
                                Log.i(selectedDate.value, "Selected Date Future Value")
                                val weatherData = weatherDatabaseAccessObject.getWeatherData(
                                    city.name, selectedDate.value
                                )
                                if (weatherData != null) {
                                    minTemp = weatherData.min_temp
                                    maxTemp = weatherData.max_temp
                                    Log.i("Already Exists", "Weather data already exists")
                                    return@launch
                                }
                                var dateStr = "";
                                val date = selectedDateCalendar.value.clone() as Calendar
                                var temp_min = 0.0
                                var temp_max = 0.0
                                for (i in 1..10) {
                                    date.add(Calendar.YEAR, -1)
                                    if (date.get(Calendar.MONTH) < 9) {
                                        if (date.get(Calendar.DAY_OF_MONTH) < 10) {
                                            dateStr =
                                                "${date.get(Calendar.YEAR)}-0${date.get(Calendar.MONTH) + 1}-0${
                                                    date.get(Calendar.DAY_OF_MONTH)
                                                }"
                                        } else {
                                            dateStr =
                                                "${date.get(Calendar.YEAR)}-0${date.get(Calendar.MONTH) + 1}-${
                                                    date.get(Calendar.DAY_OF_MONTH)
                                                }"
                                        }
                                    } else {
                                        if (date.get(Calendar.DAY_OF_MONTH) < 10) {
                                            dateStr =
                                                "${date.get(Calendar.YEAR)}-${date.get(Calendar.MONTH) + 1}-0${
                                                    date.get(Calendar.DAY_OF_MONTH)
                                                }"
                                        } else {
                                            dateStr =
                                                "${date.get(Calendar.YEAR)}-${date.get(Calendar.MONTH) + 1}-${
                                                    date.get(Calendar.DAY_OF_MONTH)
                                                }"
                                        }
                                    }
                                    Log.i(dateStr, "Date")
                                    val weather = returnWeatherData(
                                        city.latitude, city.longitude, dateStr, dateStr
                                    )
                                    temp_min += weather.hourly.temperature_2m.minOrNull()!!
                                    temp_max += weather.hourly.temperature_2m.maxOrNull()!!
                                }
//                                date= dateCopy
                                minTemp = temp_min / 10
                                maxTemp = temp_max / 10
                                weatherDatabaseAccessObject.upsert(
                                    WeatherDatabaseEntity(
                                        null,
                                        city.name,
                                        minTemp,
                                        maxTemp,
                                        selectedDate.value
                                    )
                                )
                            } catch (e: Exception) {
                                Log.i("WeatherResponse", "Failed to get weather information")
                            }
                        }
                    } else {

                        GlobalScope.launch {
                            try {
                                val weatherData = weatherDatabaseAccessObject.getWeatherData(
                                    city.name, selectedDate.value
                                )
                                if (weatherData != null) {
                                    minTemp = weatherData.min_temp
                                    maxTemp = weatherData.max_temp
                                    Log.i("Already Exists", "Weather data already exists")
                                    return@launch
                                }
                                Log.i(selectedDate.value, "Selected Date")
                                Log.i(selectedDateCalendar.value.time.toString(), "Selected Date")
                                weatherResponse.value = returnWeatherData(
                                    city.latitude,
                                    city.longitude,
                                    selectedDate.value,
                                    selectedDate.value
                                )
                                Log.i("WeatherResponse", "Weather information of ${city.name}")
                                weatherDatabaseAccessObject.upsert(
                                    WeatherDatabaseEntity(
                                        null,
                                        city.name,
                                        weatherResponse.value?.hourly?.temperature_2m?.minOrNull()!!,
                                        weatherResponse.value?.hourly?.temperature_2m?.maxOrNull()!!,
                                        selectedDate.value
                                    )
                                )
                                Log.i(
                                    "Inserted into Database", "Weather data inserted into database"
                                )
                            } catch (e: Exception) {
                                // Handle errors if any
                                Log.i("WeatherResponse", "Failed to get weather information")
                            }

                        }
                    }
                },
                modifier = Modifier
                    .padding(8.dp)
                    .width(300.dp)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AB7FF))
            ) {
                Text(
                    text = "Get Weather of ${city.name}",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (weatherResponse.value != null) {
            minTemp = weatherResponse.value?.hourly?.temperature_2m?.minOrNull()!!
            Text(text = "Minimum Temperature: $minTemp", fontSize = 20.sp)
            maxTemp = weatherResponse.value?.hourly?.temperature_2m?.maxOrNull()!!
            Text(text = "Maximum Temperature: $maxTemp", fontSize = 20.sp)
        } else {
            Text(text = "Minimum Temperature: $minTemp", fontSize = 20.sp)
            Text(text = "Maximum Temperature: $maxTemp", fontSize = 20.sp)
        }
    }
}


private fun getMinDateInMillis(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, 1940)
    calendar.set(Calendar.MONTH, Calendar.JANUARY)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    return calendar.timeInMillis
}

// Print the date selected by the user
@Composable
fun printSelectedDate(selectedDate: String) {
    Text(
        text = "Selected Date: $selectedDate",
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

@Preview
@Composable
fun PreviewMinAndMaxTemp() {
    val weatherResponse = WeatherResponse(
        0,
        0.0,
        Hourly(listOf(10.0, 20.0, 30.0), listOf("2022-01-01", "2022-01-02", "2022-01-03")),
        HourlyUnits("Celsius", "Celsius"),
        0.0,
        0.0,
        "UTC",
        "UTC",
        0
    )
}

@Preview(device = "id:pixel_4a")
@Composable
fun PreviewDatePickerButton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DatePickerButton()
    }
}

@Composable
fun WeatherButtons(date: String = "2022-01-01") {
    val cityList = listOf(
        City("New Delhi", 28.6139, 77.2090),
        City("London", 51.5074, -0.1278),
        City("New York", 40.7128, -74.0060),
    )
    val weatherResponse = remember { mutableStateOf<WeatherResponse?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        cityList.forEach { city ->
            Button(
                onClick = {
                    GlobalScope.launch {
                        try {
                            weatherResponse.value = returnWeatherData(
                                city.latitude, city.longitude, date, date
                            )
                            Log.i("WeatherResponse", "Weather information of ${city.name}")
                        } catch (e: Exception) {
                            // Handle errors if any
                            Log.i("WeatherResponse", "Failed to get weather information")
                        }

                    }

                }, modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Get Weather of ${city.name}",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        val minTemp = weatherResponse.value?.hourly?.temperature_2m?.minOrNull()
        Text(text = "Minimum Temperature: $minTemp", fontSize = 20.sp)
        val maxTemp = weatherResponse.value?.hourly?.temperature_2m?.maxOrNull()
        Text(text = "Maximum Temperature: $maxTemp", fontSize = 20.sp)
    }
}


data class City(val name: String, val latitude: Double, val longitude: Double)

@Preview
@Composable
fun PreviewWeatherButtons() {
    WeatherButtons("2022-01-01")
}
