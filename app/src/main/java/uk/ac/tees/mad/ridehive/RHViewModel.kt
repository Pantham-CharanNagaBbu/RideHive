package uk.ac.tees.mad.ridehive

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.Cloudinary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import uk.ac.tees.mad.ridehive.display.Destination
import uk.ac.tees.mad.ridehive.model.Ride
import uk.ac.tees.mad.ridehive.model.Users
import uk.ac.tees.mad.ridehive.room.RideRoom
import uk.ac.tees.mad.ridehive.room.RidesDao
import java.io.File
import javax.inject.Inject
import kotlin.jvm.java

@HiltViewModel
class RHViewModel @Inject
constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val ridesDao : RidesDao
): ViewModel() {

    val userLogin = mutableStateOf(false)
    val currentUser = MutableStateFlow<Users?>(null)
    private val _rides = MutableStateFlow<List<RideRoom>>(emptyList())
    val rides: StateFlow<List<RideRoom>> = _rides.asStateFlow()

    sealed class AuthEvent {
        data class Success(val message: String) : AuthEvent()
        data class Error(val error: String) : AuthEvent()
        object Loading : AuthEvent()
    }

    private val _authEvents = Channel<AuthEvent>()
    val authEvents = _authEvents.receiveAsFlow()

    init {
        if (auth.currentUser != null) {
            viewModelScope.launch {
                _authEvents.send(AuthEvent.Success("Already logged in"))
            }
            userLogin.value = true
            fetchRides()
            fetchCurrentUser()
        }
    }

    private val cloudinaryConfig = hashMapOf(
        "cloud_name" to "dxz1u2r4o",
        "api_key" to "762543111378498",
        "api_secret" to "dLnezlfOSHNaRKQuyQ224TcOBsY"
    )

    private val cloudinary = Cloudinary(cloudinaryConfig)

    fun delete(ride: RideRoom, context: Context, onDelete: ()-> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("rides")
                    .document(ride.rideId)
                    .delete()
                    .await()


                fetchRides()
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                onDelete()

            } catch (e: Exception) {
                Log.e("RHViewModel", "Failed to delete ride: ${e.message}")
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            tempFile.outputStream().use { output -> inputStream.copyTo(output) }
            Log.d("ComplaintViewModel", "File created: ${tempFile.absolutePath}")
            tempFile
        } catch (e: Exception) {
            Log.e("ComplaintViewModel", "Error in getFileFromUri: ${e.message}", e)
            null
        }
    }

    fun updateProfile(
        context: Context,
        firstName: String,
        lastName: String,
        photoUri: Uri? = null,
        photoBitmap: android.graphics.Bitmap? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        val firebaseUser = auth.currentUser ?: return onResult(false, "User not logged in")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var photoUrl: String? = null

                if (photoUri != null) {
                    Log.d("RHViewModel", "Uploading from URI: $photoUri")
                    val file = getFileFromUri(context, photoUri)
                    if (file != null) {
                        val uploadResult = cloudinary.uploader()
                            .upload(file, emptyMap<String, String>())
                        photoUrl = uploadResult["secure_url"] as String
                        Log.d("RHViewModel", "Uploaded URI, got URL: $photoUrl")
                    }
                }

                if (photoBitmap != null) {
                    Log.d("RHViewModel", "Uploading from Bitmap")
                    val tempFile = File.createTempFile("camera_upload_", ".jpg", context.cacheDir)
                    tempFile.outputStream().use { out ->
                        photoBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    val uploadResult = cloudinary.uploader()
                        .upload(tempFile, emptyMap<String, String>())
                    photoUrl = uploadResult["secure_url"] as String
                    Log.d("RHViewModel", "Uploaded Bitmap, got URL: $photoUrl")
                }

                val updates = mutableMapOf<String, Any>(
                    "firstName" to firstName,
                    "lastName" to lastName
                )
                if (photoUrl != null) updates["photoUrl"] = photoUrl

                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .update(updates)
                    .await()

                fetchCurrentUser()

                withContext(Dispatchers.Main) {
                    onResult(true, "Profile updated successfully")
                }
            } catch (e: Exception) {
                Log.e("RHViewModel", "Profile update failed", e)
                withContext(Dispatchers.Main) {
                    onResult(false, e.message ?: "Unknown error")
                }
            }
        }
    }

    fun logout(){
        auth.signOut()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            firestore.collection("users")
                .document(auth.currentUser?.uid ?: "")
                .get()
                .addOnSuccessListener {
                    val user = it.toObject(Users::class.java)
                    if (user != null) {
                        currentUser.value = user
                    }
                }
                .addOnFailureListener {
                    Log.e("RHViewModel", "Error fetching current user: ${it.message}")
                }
        }
    }
    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _authEvents.send(AuthEvent.Loading)

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    viewModelScope.launch {
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: return@launch

                            val userMap = mapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "email" to email,
                                "createdAt" to System.currentTimeMillis(),
                                "photoUrl" to null,
                            )

                            firestore.collection("users")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener {
                                    firestore.collection("users").document(userId).update("uid", userId)
                                        .addOnSuccessListener {
                                            viewModelScope.launch {
                                                _authEvents.send(AuthEvent.Success("Account created successfully"))
                                                fetchCurrentUser()
                                                fetchRides()
                                            }
                                        }.addOnFailureListener {
                                            viewModelScope.launch {
                                                _authEvents.send(AuthEvent.Error(it.message ?: "Failed to save user"))
                                            }
                                        }
                                }
                                .addOnFailureListener { e ->
                                    viewModelScope.launch {
                                        _authEvents.send(AuthEvent.Error(e.message ?: "Failed to save user"))
                                    }
                                }
                        } else {
                            _authEvents.send(AuthEvent.Error(task.exception?.message ?: "Sign up failed"))
                        }
                    }
                }
        }
    }

    fun fetchRides() {
        viewModelScope.launch {
            try {
                val firestoreRides = firestore.collection("rides")
                    .get()
                    .await()
                    .toObjects(Ride::class.java)
                Log.d("RideFirestore", firestoreRides.toString())
                val roomRides = firestoreRides.mapNotNull { it?.toRideRoom() }
                ridesDao.deleteAllRides()
                ridesDao.insertRide(roomRides)

                _rides.value = ridesDao.getAllRides()
                Log.d("Ride", _rides.value.toString())
            } catch (e: Exception) {
                Log.e("RHViewModel", "Error fetching rides: ${e.message}")
                _rides.value = ridesDao.getAllRides()
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authEvents.send(AuthEvent.Loading)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    viewModelScope.launch {
                        if (task.isSuccessful) {
                            _authEvents.send(AuthEvent.Success("Login successful"))
                            fetchCurrentUser()
                            fetchRides()
                        } else {
                            _authEvents.send(AuthEvent.Error(task.exception?.message ?: "Login failed"))
                        }
                    }
                }
        }
    }

    fun onPostRideClick(
        context : Context,
        current: String,
        destination: Destination,
        date: String,
        time: String,
        seats: String,
        onSuccess : () -> Unit,
        onError  : () -> Unit
    ) {
        viewModelScope.launch {
            firestore.collection("rides").add(
                mapOf(
                    "userUid" to auth.currentUser?.uid,
                    "userName" to currentUser.value?.firstName + " " + currentUser.value?.lastName,
                    "from" to current,
                    "destinationName" to destination.name,
                    "destinationLatitude" to destination.lat,
                    "destinationLongitude" to destination.lng,
                    "date" to date,
                    "time" to time,
                    "seats" to seats.toInt(),
                    "joinedUsers" to emptyList<String>(),
                )
            ).addOnSuccessListener {
                firestore.collection("rides").document(it.id).update("rideId", it.id)
                    .addOnSuccessListener {
                        onSuccess()
                        fetchRides()
                        Toast.makeText(context, "Ride posted successfully", Toast.LENGTH_SHORT)
                            .show()
                    }.addOnFailureListener {
                        onError()
                        Toast.makeText(context, "Failed to post ride", Toast.LENGTH_SHORT).show()
                    }
                    }.addOnFailureListener {
                onError()
                Toast.makeText(context, "Failed to post ride", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addMetoRide(context: Context, rideId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            firestore.collection("rides").document(rideId)
                .update("joinedUsers", FieldValue.arrayUnion(auth.currentUser?.uid))
                .addOnSuccessListener {
                    onSuccess()
                    fetchRides()
                    Toast.makeText(context, "Joined ride successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to join ride", Toast.LENGTH_SHORT).show()
                }
        }
    }
    suspend fun fetchRidebyID(context: Context,id: String): Ride? {
        return try {
            val document = firestore.collection("rides")
                .document(id)
                .get()
                .await()
            document.toObject(Ride::class.java)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to fetch ride", Toast.LENGTH_SHORT).show()
            Log.e("RHViewModel", "Error fetching ride: ${e.message}")
            null
        }
    }

    fun Ride?.toRideRoom(): RideRoom? {
        if (this == null) return null
        return RideRoom(
            rideId = this.rideId,
            userUid = this.userUid,
            userName = this.userName,
            from = this.from,
            destinationName = this.destinationName,
            destinationLatitude = this.destinationLatitude,
            destinationLongitude = this.destinationLongitude,
            date = this.date,
            time = this.time,
            seats = this.seats
        )
    }

}