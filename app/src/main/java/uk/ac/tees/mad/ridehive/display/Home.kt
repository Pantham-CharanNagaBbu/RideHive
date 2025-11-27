package uk.ac.tees.mad.ridehive.display

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import uk.ac.tees.mad.ridehive.CustomBottomNavBar
import uk.ac.tees.mad.ridehive.RHViewModel
import uk.ac.tees.mad.ridehive.model.Ride
import uk.ac.tees.mad.ridehive.navigation
import uk.ac.tees.mad.ridehive.ui.theme.*
import uk.ac.tees.mad.ridehive.utilities.calculateDistance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    navController: NavController,
    viewModel: RHViewModel = hiltViewModel()
) {
    val rides = viewModel.rides.value

    val context = LocalContext.current
    var isLocationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isLocationPermissionGranted = isGranted
    }

    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(isLocationPermissionGranted) {
        if (isLocationPermissionGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLat = it.latitude
                    currentLng = it.longitude
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RideHive",
                        color = RideHivePrimary,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            )
        },
        bottomBar = {
            CustomBottomNavBar(
                selectedRoute = navigation.Home.route,
                navController = navController
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RideHiveBackground)
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!isLocationPermissionGranted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Location permission is required to find rides near you.",
                        color = RideHiveTextPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    context as Activity,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )) {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            } else {
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                                    val uri = Uri.fromParts("package", context.packageName, null)
                                    it.data = uri
                                    context.startActivity(it)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RideHivePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text(
                            text = "Grant Location Permission",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Text(
                text = "Available Rides",
                style = MaterialTheme.typography.titleMedium,
                color = RideHiveTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(rides) { ride ->
                    RideCard(ride, currentLat, currentLng)
                }
            }
        }
    }
}

@Composable
fun RideCard(
    ride: Ride,
    currentLat: Double?,
    currentLng: Double?
) {
    // Pickup distance (your location → ride.from)
    val pickupDistance = remember(currentLat, currentLng, ride) {
        if (currentLat != null && currentLng != null && ride.from.isNotBlank()) {
            // Assuming ride.from contains "lat,lng" string
            val parts = ride.from.split(",")
            if (parts.size == 2) {
                val fromLat = parts[0].toDoubleOrNull()
                val fromLng = parts[1].toDoubleOrNull()
                if (fromLat != null && fromLng != null) {
                    val dist = calculateDistance(currentLat, currentLng, fromLat, fromLng)
                    "%.2f km".format(dist)
                } else "N/A"
            } else "N/A"
        } else "N/A"
    }

    val destinationDistance = remember(currentLat, currentLng, ride) {
        if (currentLat != null && currentLng != null &&
            ride.destinationLatitude != null && ride.destinationLongitude != null
        ) {
            val dist = calculateDistance(
                currentLat,
                currentLng,
                ride.destinationLatitude,
                ride.destinationLongitude
            )
            "%.2f km".format(dist)
        } else "N/A"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = RideHivePrimary)
                Text(
                    text = ride.userName,
                    style = MaterialTheme.typography.titleMedium,
                    color = RideHiveTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = RideHiveSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Pickup: ${ride.from} → ${ride.destinationName}",
                    color = RideHiveTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = RideHivePrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Time: ${ride.time}", fontSize = 14.sp, color = RideHiveTextSecondary)
                }
                Text(
                    "${ride.seats} seats left",
                    color = RideHiveSuccess,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Route, contentDescription = null, tint = RideHiveSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Pickup Distance: $pickupDistance", color = RideHiveTextSecondary)
                    Text("Destination Distance: $destinationDistance", color = RideHiveTextSecondary)
                }
            }
        }
    }
}

