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
import uk.ac.tees.mad.ridehive.display.SignUp
import uk.ac.tees.mad.ridehive.display.Splash
import uk.ac.tees.mad.ridehive.ui.theme.RideHiveTheme

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


sealed class navigation(val route : String){
    object Splash : navigation("splash")
    object Login : navigation("login")
    object SignUp : navigation("signup")

}

@Composable
fun RideHive(innerPadding: PaddingValues) {
    NavHost(rememberNavController(), startDestination = navigation.SignUp.route){
        composable(navigation.Splash.route){
            Splash(innerPadding)
        }
        composable(navigation.Login.route){
            //Login()
        }
        composable(navigation.SignUp.route){
            SignUp()
        }
    }
}