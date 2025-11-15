package uk.ac.tees.mad.ridehive.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.ridehive.ui.theme.RideHiveBackground
import uk.ac.tees.mad.ridehive.ui.theme.RideHivePrimary
import uk.ac.tees.mad.ridehive.ui.theme.RideHiveSecondary
import uk.ac.tees.mad.ridehive.ui.theme.RideHiveTextSecondary
import uk.ac.tees.mad.ridehive.ui.theme.RideHiveTextPrimary

@Composable
fun Login(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onNavigateToSignUp: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isFormValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            password.length >= 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // 🔹 Branding / Title
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

        // 🔹 Email
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

        // 🔹 Password
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

        // 🔹 Login Button
        Button(
            onClick = { onLoginClick(email, password) },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RideHivePrimary,
                contentColor = RideHiveBackground
            )
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Navigate to Sign Up
        TextButton(onClick = onNavigateToSignUp) {
            Text(
                text = "Don’t have an account? Sign up",
                color = RideHiveSecondary
            )
        }
    }
}
