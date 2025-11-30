package uk.ac.tees.mad.ridehive

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.ridehive.display.Destination
import uk.ac.tees.mad.ridehive.model.Ride
import uk.ac.tees.mad.ridehive.model.Users
import javax.inject.Inject
import kotlin.jvm.java

@HiltViewModel
class RHViewModel @Inject
constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
): ViewModel() {

    val userLogin = mutableStateOf(false)
    val currentUser = mutableStateOf<Users?>(null)
    val rides = mutableStateOf<List<Ride>>(emptyList())

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
                                "createdAt" to System.currentTimeMillis()
                            )

                            firestore.collection("users")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener {
                                    viewModelScope.launch {
                                        _authEvents.send(AuthEvent.Success("Account created successfully"))
                                        fetchCurrentUser()
                                        fetchRides()
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

    fun fetchRides(){
        viewModelScope.launch {
            firestore.collection("rides")
                .get().addOnSuccessListener {
                    rides.value = it.toObjects(Ride::class.java)
                }.addOnFailureListener {
                    Log.e("RHViewModel", "Error fetching rides: ${it.message}")
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

}