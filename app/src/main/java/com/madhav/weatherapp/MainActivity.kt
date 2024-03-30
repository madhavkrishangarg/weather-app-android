package com.madhav.weatherapp

import android.app.DatePickerDialog
import android.os.Bundle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContent {
            WeatherApp()
        }
    }
}
@Preview
@Composable
fun WeatherApp() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DatePickerButton()
        WeatherButtons()
    }
}

@Composable
fun DatePickerButton() {
    val context = LocalContext.current
    val selectedDate = remember { mutableStateOf("") }
    Button(
        onClick = {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, day ->
                    if (year >= 1940) {
                        selectedDate.value = "$day/${month + 1}/$year"
                        Toast.makeText(context, selectedDate.value, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please select a date after 1940", Toast.LENGTH_SHORT).show()
                    }
                },
                currentYear,
                currentMonth,
                currentDay
            )

            datePickerDialog.datePicker.minDate = getMinDateInMillis()
            datePickerDialog.show()
        },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Select Date",
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    }
    printSelectedDate(selectedDate.value)
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

//create a dropdown menu to select the city
@Composable
fun DropdownList(itemList: List<String>, selectedIndex: Int, modifier: Modifier, onItemClick: (Int) -> Unit) {

    var showDropdown by rememberSaveable { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        // button
        Box(
            modifier = modifier
                .background(Color.Red)
                .clickable { showDropdown = true },
//            .clickable { showDropdown = !showDropdown },
            contentAlignment = Alignment.Center
        ) {
            Text(text = itemList[selectedIndex], modifier = Modifier.padding(3.dp))
        }

        // dropdown list
        Box() {
            if (showDropdown) {
                Popup(
                    alignment = Alignment.TopCenter,
                    properties = PopupProperties(
                        excludeFromSystemGesture = true,
                    ),
                    // to dismiss on click outside
                    onDismissRequest = { showDropdown = false }
                ) {

                    Column(
                        modifier = modifier
                            .heightIn(max = 90.dp)
                            .verticalScroll(state = scrollState)
                            .border(width = 1.dp, color = Color.Gray),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        itemList.onEachIndexed { index, item ->
                            if (index != 0) {
                                Divider(thickness = 1.dp, color = Color.LightGray)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.Green)
                                    .fillMaxWidth()
                                    .clickable {
                                        onItemClick(index)
                                        showDropdown = !showDropdown
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item,)
                            }
                        }

                    }
                }
            }
        }
    }

}

@Composable
fun cityDropdown() {
    val cityList = listOf("New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose")
    var selectedIndex by remember { mutableStateOf(0) }

    DropdownList(
        itemList = cityList,
        selectedIndex = selectedIndex,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        onItemClick = { index ->
            selectedIndex = index
        }
    )
}

//create three buttons with city name, whenever user clicks on the button, it should call an API to get the weather information of that city and display it
//@Composable
//fun WeatherButtons() {
//    val cityList = listOf("New York", "Los Angeles", "Chicago")
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        cityList.forEach { city ->
//            Button(
//                onClick = {
//                    scope.launch {
//                        try {
//                            // Simulate API call delay
//                            delay(1000)
//                            // Replace the following line with your actual API call
//                            Toast.makeText(context, "Weather information of $city", Toast.LENGTH_SHORT).show()
//                        } catch (e: Exception) {
//                            // Handle errors if any
//                            Toast.makeText(context, "Failed to get weather information", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .padding(16.dp)
//                    .fillMaxWidth()
//            ) {
//                Text(
//                    text = "Get Weather of $city",
//                    fontSize = 20.sp,
//                    textAlign = TextAlign.Center
//                )
//            }
//        }
//    }
//}

@Serializable
data class WeatherResponse(val hourly: Map<String, List<Double>>)

@Composable
fun WeatherButtons() {
    val cityList = listOf(
        City("New York", 40.7128, -74.0060),
        City("Los Angeles", 34.0522, -118.2437),
        City("Chicago", 41.8781, -87.6298)
    )
    val scope = rememberCoroutineScope()
    val client = HttpClient()

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
                    scope.launch {
                        try {
                            val response: WeatherResponse = client.get("https://archive-api.open-meteo.com/v1/archive") {
                                parameter("latitude", city.latitude)
                                parameter("longitude", city.longitude)
                                parameter("start_date", "2024-03-28")
                                parameter("end_date", "2024-03-28")
                                parameter("hourly", "temperature_2m")
                            }
                            Toast.makeText(LocalContext.current, "Weather information of ${city.name}: ${response.hourly}", Toast.LENGTH_SHORT).show()
                            println("Weather information of ${city.name}: ${response.hourly}")
                        } catch (e: Exception) {
                            println("Failed to get weather information: ${e.message}")
                        }
                    }
                },
                modifier = Modifier
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
    }
}

data class City(val name: String, val latitude: Double, val longitude: Double)

@Preview
@Composable
fun PreviewWeatherButtons() {
    WeatherButtons()
}