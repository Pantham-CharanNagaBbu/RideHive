package uk.ac.tees.mad.ridehive

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.ac.tees.mad.ridehive.display.Splash
import uk.ac.tees.mad.ridehive.ui.theme.RideHiveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RideHiveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Splash(innerPadding)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val osName = System.getProperty("os.name")
    val cores = Runtime.getRuntime().availableProcessors()
    val deviceModel = Build.MODEL
    val deviceCpu = Build.SUPPORTED_ABIS.joinToString(", ")

    Text(
        text = "Hello $name! I am running on $osName with $cores cores.\n" +
                "Device: $deviceModel\nCPU Architectures: $deviceCpu",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RideHiveTheme {
        Greeting("Android")
    }
}