package uk.ac.tees.mad.ridehive.display

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
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
        modifier = Modifier.systemBarsPadding(),
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
                val shouldJoin = ride!!.joinedUsers.size < ride!!.seats
                Button(
                    onClick = {
                        viewModel.addMetoRide(context,rideID, onSuccess = {
                            navController.popBackStack()
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RideHivePrimary,
                        contentColor = Color.White
                    ),
                    enabled = shouldJoin
                ) {
                    if (shouldJoin){
                        Text("Join Ride")
                    }else{
                        Text("Full")
                    }
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = RideHiveSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Pickup: ${ride.from}", fontSize = 14.sp, color = RideHiveTextSecondary)
                    Text("Destination: ${ride.destinationName}", fontSize = 14.sp, color = RideHiveTextSecondary)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = RideHivePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${ride.date} at ${ride.time}", fontSize = 14.sp, color = RideHiveTextSecondary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EventSeat, contentDescription = null, tint = RideHivePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${ride.seats} seat(s) available", fontSize = 14.sp, color = RideHiveTextSecondary)
            }

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

@Preview(showBackground = true, name = "RideHive – Ride Detail")
@Composable
fun RideDetailScreenPreview() {
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
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                "Ride Details",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(32.dp))

        // Ride Detail Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = Color(0xFFE63946))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Emma Wilson",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                HorizontalDivider(color = Color.LightGray)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF778DA9))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Pickup: Your Current Location", color = Color(0xFF666666))
                        Text("Destination: Teesside University", color = Color(0xFF666666))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color(0xFFE63946))
                    Spacer(Modifier.width(12.dp))
                    Text("17 Sep 2025 at 10:30 AM", color = Color(0xFF666666))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventSeat, null, tint = Color(0xFFE63946))
                    Spacer(Modifier.width(12.dp))
                    Text("3 seat(s) available", color = Color(0xFF666666))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MyLocation, null, tint = Color(0xFFE63946))
                    Spacer(Modifier.width(12.dp))
                    Text("Pickup is 1.2 km from you", color = Color(0xFF666666))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, null, tint = Color(0xFF778DA9))
                    Spacer(Modifier.width(12.dp))
                    Text("Destination is 3.8 km from you", color = Color(0xFF666666))
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Join Ride Button
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
        ) {
            Text("Join Ride", color = Color.White, fontSize = 18.sp)
        }

        Spacer(Modifier.height(100.dp)) // Bottom padding
    }
}