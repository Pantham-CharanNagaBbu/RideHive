package uk.ac.tees.mad.ridehive.display

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun Detail(navController: NavHostController, rideID: String) {
    Text(rideID)
}