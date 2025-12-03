package uk.ac.tees.mad.ridehive.display

import androidx.compose.runtime.Composable

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import uk.ac.tees.mad.ridehive.CustomBottomNavBar
import uk.ac.tees.mad.ridehive.RHViewModel
import uk.ac.tees.mad.ridehive.navigation
import uk.ac.tees.mad.ridehive.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    navController: NavController,
    viewModel: RHViewModel = hiltViewModel()
) {
    val user = viewModel.currentUser.collectAsState().value
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = RideHivePrimary) }
            )
        },
        bottomBar = {
            CustomBottomNavBar(
                selectedRoute = navigation.Profile.route,
                navController = navController
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RideHiveBackground)
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = rememberAsyncImagePainter(user?.photoUrl ?: ""),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(user?.firstName ?: "Unknown User", style = MaterialTheme.typography.titleMedium)
            Text(user?.lastName ?: "Unknown User", style = MaterialTheme.typography.titleMedium)
            Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = RideHiveTextSecondary)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showEditDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = RideHivePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Profile", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { //onLogout()
                          },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Logout", color = Color.Red)
            }
        }

        if (showEditDialog) {
            EditProfileDialog(
                currentName = user?.firstName ?: "",
                onDismiss = { showEditDialog = false },
                onUpdate = { newName, newPhoto ->
                    //viewModel.updateProfile(newName, newPhoto)
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onUpdate: (String, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    // Camera picker
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Convert bitmap to Uri if you need upload support
        // For now just ignore or save temp
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = RideHiveSecondary)
                    ) {
                        Text("Gallery", color = Color.White)
                    }
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = RideHiveSecondary)
                    ) {
                        Text("Camera", color = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdate(name, photoUri) },
                colors = ButtonDefaults.buttonColors(containerColor = RideHivePrimary)
            ) {
                Text("Update", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}