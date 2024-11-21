package com.example.geolocationapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                GeolocationScreen()
            }
        }
    }
}

@Composable
fun GeolocationScreen() {
    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var latitude by remember { mutableStateOf<String?>(null) }
    var longitude by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFetchingLocation by remember { mutableStateOf(false) } // Состояние для управления отображением

    val locationPermissionState = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                errorMessage = null
                isFetchingLocation = true
            } else {
                errorMessage = "Доступ к геолокации не предоставлен."
            }
        }
    )

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                latitude = location.latitude.toString()
                longitude = location.longitude.toString()
                errorMessage = null
            } else {
                errorMessage = "Не удалось получить геолокацию."
            }
            isFetchingLocation = false // Завершаем загрузку
        }
    }

    // Логика получения геолокации активируется только после нажатия кнопки
    if (isFetchingLocation) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLocationWithPermission(locationClient, locationCallback)
        } else {
            locationPermissionState.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    GeolocationContent(
        latitude = latitude,
        longitude = longitude,
        errorMessage = errorMessage,
        onRequestLocation = {
            locationPermissionState.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        },
        isFetchingLocation = isFetchingLocation
    )
}

@SuppressLint("MissingPermission")
fun getLocationWithPermission(
    client: FusedLocationProviderClient,
    locationCallback: LocationCallback
) {
    val locationRequest = LocationRequest.Builder(
        5000
    ).build()

    client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
}

@Composable
fun GeolocationContent(
    latitude: String?,
    longitude: String?,
    errorMessage: String?,
    onRequestLocation: () -> Unit,
    isFetchingLocation: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            latitude != null && longitude != null -> {
                Text(
                    text = "Широта: $latitude\nДолгота: $longitude",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }

            isFetchingLocation -> {
                CircularProgressIndicator() // Индикатор загрузки
            }

            else -> {
                Button(onClick = onRequestLocation) {
                    Text(text = "Получить координаты")
                }
            }
        }
    }
}