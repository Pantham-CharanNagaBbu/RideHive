package uk.ac.tees.mad.ridehive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import uk.ac.tees.mad.ridehive.display.Home
import uk.ac.tees.mad.ridehive.display.Login
import uk.ac.tees.mad.ridehive.display.SignUp
import uk.ac.tees.mad.ridehive.display.Splash
import uk.ac.tees.mad.ridehive.ui.theme.RideHiveTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.tooling.preview.Preview

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RideHiveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RideHive(innerPadding)
                }
            }
        }
    }
}

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun CustomBottomNavBar(
    selectedRoute: String,
    onNavItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        NavItem("Home", Icons.Default.Home, navigation.Home.route),
        NavItem("Post", Icons.Default.Add, ""),
        NavItem("My Rides", Icons.Default.Route, ""),
        NavItem("Profile", Icons.Default.Person, "")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedRoute == item.route) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .padding(12.dp)
                    ) {
                        IconButton(onClick = { onNavItemClick(item.route) }) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selectedRoute == item.route) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
            }
        }
    }
}

@Composable
@Preview
fun CustomBottomNavBarPreview() {
    MaterialTheme {
        CustomBottomNavBar(
            selectedRoute = "home",
            onNavItemClick = { /* Handle click */ }
        )
    }
}

sealed class navigation(val route : String){
    object Splash : navigation("splash")
    object Login : navigation("login")
    object SignUp : navigation("signup")
    object Home : navigation("home")

}

@Composable
fun RideHive(innerPadding: PaddingValues) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = navigation.Splash.route){
        composable(navigation.Splash.route){
            Splash(innerPadding,
                navController)
        }
        composable(navigation.Login.route){
            Login(
                navController = navController,
                onNavigateToSignUp = {
                    navController.navigate(navigation.SignUp.route){
                        popUpTo(0)
                    }
                }
            )
        }
        composable(navigation.SignUp.route){
            SignUp(
                navController = navController,
                onNavigateToLogin = {
                    navController.navigate(navigation.Login.route){
                        popUpTo(0)
                    }
                }
            )
        }
        composable(navigation.Home.route){
            Home()
        }
    }
}