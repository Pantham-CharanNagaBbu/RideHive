package uk.ac.tees.mad.ridehive.display

import android.content.Context
import androidx.compose.runtime.Composable

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        },
        modifier = Modifier.systemBarsPadding()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RideHiveBackground)
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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

            Text(
                "${user?.firstName ?: ""} ${user?.lastName ?: ""}",
                style = MaterialTheme.typography.titleMedium
            )
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
                onClick = {
                    navController.navigate(navigation.Login.route){
                        popUpTo(0)
                    }
                    viewModel.logout() },
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
                currentFirstName = user?.firstName ?: "",
                currentLastName = user?.lastName ?: "",
                currentPhotoUrl = user?.photoUrl,
                onDismiss = { showEditDialog = false },
                viewModel,
                context = LocalContext.current
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentFirstName: String,
    currentLastName: String,
    currentPhotoUrl: String?,
    onDismiss: () -> Unit,
    viewModel: RHViewModel = hiltViewModel(),
    context: Context,
    ) {
    var firstName by remember { mutableStateOf(currentFirstName) }
    var lastName by remember { mutableStateOf(currentLastName) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var previewBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        previewBitmap = bitmap
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    if (photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(currentPhotoUrl ?: ""),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp)
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
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
                onClick = {
                    if (firstName.isBlank() || lastName.isBlank()) {
                        Toast.makeText(context, "Please enter name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (photoUri == null && previewBitmap == null) {
                        viewModel.updateProfile(
                            context = context,
                            firstName = firstName,
                            lastName = lastName,
                            photoUri = null,
                            photoBitmap = null
                        ) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) onDismiss()
                        }
                    } else {
                        viewModel.updateProfile(
                            context = context,
                            firstName = firstName,
                            lastName = lastName,
                            photoUri = photoUri,
                            photoBitmap = previewBitmap
                        ) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) onDismiss()
                        }
                    }
                }
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
@Preview(showBackground = true, name = "RideHive – Profile Screen")
@Composable
fun ProfileScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF1B98E0)),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Profile",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(Modifier.height(40.dp))

        // Profile Picture
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF415A77)),
            contentAlignment = Alignment.Center
        ) {
            Text("JD", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        Text("John", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Doe", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("john.doe@example.com", fontSize = 16.sp, color = Color(0xFF778DA9))

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Edit Profile", color = Color.White)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
            Spacer(Modifier.width(8.dp))
            Text("Logout", color = Color.Red)
        }

        Spacer(Modifier.height(40.dp))

        // Bottom nav placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFF1B263B))
        )
    }
}