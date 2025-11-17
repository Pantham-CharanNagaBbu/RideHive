package uk.ac.tees.mad.ridehive.display

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest
import uk.ac.tees.mad.ridehive.RHViewModel
import uk.ac.tees.mad.ridehive.navigation
import uk.ac.tees.mad.ridehive.ui.theme.*

@Composable
fun Login(
    onNavigateToSignUp: () -> Unit,
    navController: NavHostController,
    viewModel: RHViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isFormValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            password.length >= 6

    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.authEvents.collectLatest { event ->
            when (event) {
                is RHViewModel.AuthEvent.Loading -> {
                    isLoading = true
                }
                is RHViewModel.AuthEvent.Success -> {
                    isLoading = false
                    snackbarHostState.showSnackbar(event.message)
                    navController.navigate(navigation.Home.route)
                }
                is RHViewModel.AuthEvent.Error -> {
                    isLoading = false
                    snackbarHostState.showSnackbar(event.error)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RideHive",
                style = MaterialTheme.typography.headlineLarge,
                color = RideHivePrimary
            )
            Text(
                text = "Welcome back, log in",
                style = MaterialTheme.typography.bodyLarge,
                color = RideHiveTextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = RideHiveTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = RideHiveTextPrimary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = RideHiveTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = RideHiveTextPrimary),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, contentDescription = null, tint = RideHivePrimary)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(email, password) },
                enabled = isFormValid && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RideHivePrimary,
                    contentColor = RideHiveBackground
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = RideHiveBackground,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToSignUp) {
                Text(
                    text = "Don’t have an account? Sign up",
                    color = RideHiveSecondary
                )
            }
        }
    }
}
