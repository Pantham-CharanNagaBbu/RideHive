package uk.ac.tees.mad.ridehive.display

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import uk.ac.tees.mad.ridehive.RHViewModel
import uk.ac.tees.mad.ridehive.model.Ride
import uk.ac.tees.mad.ridehive.ui.theme.*
import uk.ac.tees.mad.ridehive.utilities.calculateDistance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Detail(
    navController: NavHostController,
    rideID: String,
    viewModel: RHViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var ride by remember { mutableStateOf<Ride?>(null) }
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }

    // Location permission
    var isLocationPermissionGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isLocationPermissionGranted = granted
        if (!granted) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    }

    // Fetch ride details and location
    LaunchedEffect(Unit) {
        ride = viewModel.fetchRidebyID(context, rideID)

        if (!isLocationPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            fused.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLat = it.latitude
                    currentLng = it.longitude
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ride Details", color = RideHiveTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (ride != null) {
                Button(
                    onClick = { /* TODO: Join Ride */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RideHivePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Join Ride")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (ride == null) {
                CircularProgressIndicator(color = RideHivePrimary)
            } else {
                RideDetailCard(ride!!, currentLat, currentLng)
            }
        }
    }
}

@Composable
fun RideDetailCard(ride: Ride, currentLat: Double?, currentLng: Double?) {
    val pickupDistance = remember(currentLat, currentLng) {
        if (currentLat != null && currentLng != null && ride.from.isNotBlank()) {
            // 🚨 assuming ride.from is stored as "lat,lng"
            val coords = ride.from.split(",")
            if (coords.size == 2) {
                val lat = coords[0].toDoubleOrNull()
                val lng = coords[1].toDoubleOrNull()
                if (lat != null && lng != null) {
                    calculateDistance(currentLat, currentLng, lat, lng)
                } else null
            } else null
        } else null
    }

    val destinationDistance = remember(currentLat, currentLng) {
        if (currentLat != null && currentLng != null &&
            ride.destinationLatitude != null && ride.destinationLongitude != null
        ) {
            calculateDistance(currentLat, currentLng, ride.destinationLatitude, ride.destinationLongitude)
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Driver Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = RideHivePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = ride.userName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = RideHiveTextPrimary
                    )
                )
            }

            Divider()

            // Route
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = RideHiveSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Pickup: ${ride.from}", fontSize = 14.sp, color = RideHiveTextSecondary)
                    Text("Destination: ${ride.destinationName}", fontSize = 14.sp, color = RideHiveTextSecondary)
                }
            }

            // Date & Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = RideHivePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${ride.date} at ${ride.time}", fontSize = 14.sp, color = RideHiveTextSecondary)
            }

            // Seats
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EventSeat, contentDescription = null, tint = RideHivePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${ride.seats} seat(s) available", fontSize = 14.sp, color = RideHiveTextSecondary)
            }

            // Distances
            if (pickupDistance != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = RideHivePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pickup is $pickupDistance km from you", fontSize = 14.sp, color = RideHiveTextSecondary)
                }
            }

            if (destinationDistance != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = RideHiveSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Destination is $destinationDistance km from you", fontSize = 14.sp, color = RideHiveTextSecondary)
                }
            }
        }
    }
}
