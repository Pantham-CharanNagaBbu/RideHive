package uk.ac.tees.mad.ridehive.display

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import uk.ac.tees.mad.ridehive.CustomBottomNavBar
import uk.ac.tees.mad.ridehive.navigation
import uk.ac.tees.mad.ridehive.ui.theme.*
import android.location.Location
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import uk.ac.tees.mad.ridehive.RHViewModel
import uk.ac.tees.mad.ridehive.utilities.calculateDistance


data class Destination(val name: String, val lat: Double, val lng: Double)

val destinations = listOf(
    Destination("Teesside University", 54.5742, -1.2354),
    Destination("Middlesbrough College", 54.5764, -1.2367),
    Destination("Cineworld Middlesbrough", 54.5639, -1.2388),
    Destination("James Cook Hospital", 54.5536, -1.2181)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostRide(
    navController: NavHostController,
    viewModel: RHViewModel = hiltViewModel(),
) {
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }

    var loading by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLat = it.latitude
                currentLng = it.longitude
            }
        }
    }

    var selectedDestination by remember { mutableStateOf<Destination?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val isFormValid = currentLat != null && selectedDestination != null &&
            date.isNotBlank() && time.isNotBlank() && seats.isNotBlank()

    val distance = remember(currentLat, currentLng, selectedDestination) {
        if (currentLat != null && currentLng != null && selectedDestination != null) {
            calculateDistance(
                currentLat!!,
                currentLng!!,
                selectedDestination!!.lat,
                selectedDestination!!.lng
            )
        } else null
    }

    Scaffold(
        bottomBar = {
            CustomBottomNavBar(
                selectedRoute = navigation.PostRide.route,
                navController = navController
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Post a Ride",
                style = MaterialTheme.typography.headlineMedium,
                color = RideHivePrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (currentLat != null) "Pickup: Your Current Location" else "Fetching location...",
                style = MaterialTheme.typography.bodyLarge,
                color = RideHiveTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedDestination?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Destination") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = RideHiveSecondary)
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    destinations.forEach { dest ->
                        DropdownMenuItem(
                            text = { Text(dest.name) },
                            onClick = {
                                selectedDestination = dest
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (distance != null) {
                Text(
                    text = "Distance: %.2f km".format(distance),
                    color = RideHivePrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (e.g. 17/09/2025)") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = RideHivePrimary)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time (e.g. 10:00 AM)") },
                leadingIcon = {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = RideHivePrimary)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = seats,
                onValueChange = { seats = it },
                label = { Text("Available Seats") },
                leadingIcon = {
                    Icon(Icons.Default.EventSeat, contentDescription = null, tint = RideHivePrimary)
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    selectedDestination?.let {
                        loading = true
                        viewModel.onPostRideClick(context,"${currentLat},${currentLng}", it, date, time, seats,
                            onSuccess={
                                currentLat = null
                                currentLng = null
                                selectedDestination = null
                                date = ""
                                time = ""
                                seats = ""
                                notes = ""
                                loading = false
                            },
                            onError={
                                loading = false
                            })
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RideHivePrimary,
                    contentColor = RideHiveBackground
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = RideHiveBackground,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Post Ride")
                }
            }
        }
    }
}
