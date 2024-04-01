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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            getWeatherData(37.7749, -122.4194, "2022-01-01", "2022-01-02")
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

@Composable
fun printSelectedDate(selectedDate: String) {
    Text(
        text = "Selected Date: $selectedDate",
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun DatePickerButton(onDateSelected: (String) -> Unit) {
    val context = LocalContext.current

    Button(
        onClick = {
            // Implement DatePickerDialog logic here
        }, modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Select Date", fontSize = 20.sp, textAlign = TextAlign.Center
        )
    }
}

data class City(val name: String, val latitude: Double, val longitude: Double)
