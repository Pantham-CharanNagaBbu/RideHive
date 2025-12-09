package uk.ac.tees.mad.ridehive.display

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
import androidx.navigation.NavController
import uk.ac.tees.mad.ridehive.CustomBottomNavBar
import uk.ac.tees.mad.ridehive.RHViewModel
import uk.ac.tees.mad.ridehive.navigation
import uk.ac.tees.mad.ridehive.room.RideRoom
import uk.ac.tees.mad.ridehive.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRides(
    navController: NavController,
    viewModel : RHViewModel = hiltViewModel()
) {
    val myUid = viewModel.currentUser.collectAsState().value?.uid
    val rides = viewModel.rides.collectAsState().value.filter {
        it.userUid == myUid
    }
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Rides", color = RideHivePrimary) }
            )
        },
        bottomBar = {
                CustomBottomNavBar(
                    selectedRoute = navigation.MyRides.route,
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
            if (rides.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You haven't posted any rides yet.",
                        color = RideHiveTextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(rides) { ride ->
                        MyRideCard(ride, onDelete = {
                            viewModel.delete(ride, context, onDelete = {navController.popBackStack()})
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun MyRideCard(ride: RideRoom, onDelete : () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = RideHivePrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ride.userName,
                        style = MaterialTheme.typography.titleMedium,
                        color = RideHiveTextPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Edit Ride",
                    tint = RideHiveSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDelete() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = RideHiveSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pickup: ${ride.from} → ${ride.destinationName}", color = RideHiveTextSecondary)
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
        }
    }
}


@Preview(showBackground = true, name = "RideHive – My Rides")
@Composable
fun MyRidesScreenPreview() {
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
                "My Rides",
                color = Color(0xFFE63946),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(32.dp))

        // Sample rides
        val myRides = listOf(
            Triple("Emma Wilson", "Teesside University", "10:30 AM", ),
            Triple("Alex Chen", "Middlesbrough College", "2:15 PM", )
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(myRides) { driver ->


                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = Color(0xFFE63946))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    driver.first,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                //tint = Color(Color(0xFF778DA9)),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { }
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF778DA9))
                            Spacer(Modifier.width(8.dp))
                            Text("Pickup: Your Location → $", color = Color(0xFF666666))
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
                                Text(driver.second, color = Color(0xFF666666))
                            }
                            Text(
                                "${driver.third} seats left",
                                color = Color(0xFF2A9D8F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}