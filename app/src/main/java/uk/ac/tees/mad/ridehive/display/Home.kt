package uk.ac.tees.mad.ridehive.display

import android.Manifest
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import uk.ac.tees.mad.ridehive.CustomBottomNavBar
import uk.ac.tees.mad.ridehive.ui.theme.*

data class Ride(
    val driver: String,
    val from: String,
    val to: String,
    val time: String,
    val seats: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    val rides = listOf(
        Ride("John Doe", "Hostel", "Teesside University", "10:00 AM", 2),
        Ride("Emma Watson", "Downtown", "Teesside University", "9:30 AM", 1),
        Ride("Michael Smith", "Station Road", "Campus Library", "11:15 AM", 3),
    )

    val context = LocalContext.current
    var isLocationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher for requesting the permission (must be defined before use)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isLocationPermissionGranted = isGranted
        if (isGranted) {
            // Handle location permission granted (e.g., start location services)
        } else {
            // Handle location permission denied (e.g., show rationale or snackbar)
        }
    }

    // Request permission if not already granted (runs once on composition)
    LaunchedEffect(Unit) {
        if (!isLocationPermissionGranted) {
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
                selectedRoute = "home",
                onNavItemClick = { /* Handle click */ }
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
            // Optional: Show permission status or conditional content here
            if (!isLocationPermissionGranted) {
                Text(
                    text = "Location permission is required for full functionality.",
                    color = RideHiveTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                    RideCard(ride)
                }
            }
        }
    }
}

@Composable
fun RideCard(ride: Ride) {
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
                    text = ride.driver,
                    style = MaterialTheme.typography.titleMedium,
                    color = RideHiveTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = RideHiveSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("${ride.from} → ${ride.to}", color = RideHiveTextSecondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = RideHivePrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Time: ${ride.time}", fontSize = 14.sp, color = RideHiveTextSecondary)
                }
                Text(
                    "${ride.seats} seats left",
                    color = RideHiveSuccess,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}