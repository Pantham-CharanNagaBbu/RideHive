package uk.ac.tees.mad.ridehive.display

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import uk.ac.tees.mad.ridehive.room.RideRoom
import uk.ac.tees.mad.ridehive.ui.theme.*
import uk.ac.tees.mad.ridehive.utilities.calculateDistance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    navController: NavController,
    viewModel: RHViewModel = hiltViewModel()
) {
    val rides = viewModel.rides.collectAsState().value

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

    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(400)
        debouncedQuery = searchQuery
    }

    val filteredRides = remember(rides, debouncedQuery) {
        if (debouncedQuery.isBlank()) rides
        else rides.filter {
            it.destinationName.contains(debouncedQuery, ignoreCase = true)
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
        },
        modifier = Modifier.systemBarsPadding()
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
                                )
                            ) {
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

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by destination...") },
                shape = RoundedCornerShape(26.dp),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp)),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RideHivePrimary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (debouncedQuery.isBlank()) "Available Rides" else "Search Results",
                style = MaterialTheme.typography.titleMedium,
                color = RideHiveTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRides) { ride ->
                    RideCard(
                        ride = ride,
                        currentLat = currentLat,
                        currentLng = currentLng,
                        onClick = { navController.navigate(navigation.Detail.createRoute(ride.rideId)) }
                    )
                }
            }
        }
    }
}


@Composable
fun RideCard(
    ride: RideRoom,
    currentLat: Double?,
    currentLng: Double?,
    onClick : () -> Unit
) {
    val pickupDistance = remember(currentLat, currentLng, ride) {
        if (currentLat != null && currentLng != null && ride.from.isNotBlank()) {
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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

@Preview(showBackground = true, name = "RideHive – Home Screen")
@Composable
fun HomeScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B263B))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RideHive",
                color = Color(0xFFE63946),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        // Search field
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search by destination...") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color(0xFFE63946)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE63946),
                unfocusedBorderColor = Color(0xFF415A77)
            )
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Available Rides",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(12.dp))

        // Sample rides
        val sampleRides = listOf(
            Triple("Emma Wilson", "Teesside University", "10:30 AM", ),
            Triple("Alex Chen", "Middlesbrough College", "11:15 AM", ),
            Triple("Sarah Khan", "Cineworld", "2:00 PM", )
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sampleRides) { index ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = Color(0xFFE63946))
                            Spacer(Modifier.width(8.dp))
                            Text(index.first, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF778DA9))
                            Spacer(Modifier.width(8.dp))
                            Text("→ ${index.second}", color = Color(0xFF778DA9))
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsCar, null, tint = Color(0xFFE63946))
                                Spacer(Modifier.width(8.dp))
                                Text("time", color = Color.Gray)
                            }
                            Text(
                                "${index.third} seats left",
                                color = Color(0xFF2A9D8F),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Route, null, tint = Color(0xFF778DA9))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Pickup Distance: 1.2 km", color = Color.Gray, fontSize = 14.sp)
                                Text("Destination Distance: 3.8 km", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
