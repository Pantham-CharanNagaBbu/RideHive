package uk.ac.tees.mad.ridehive

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RHViewModel @Inject
constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
): ViewModel() {

    val userLogin = mutableStateOf(false)

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

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authEvents.send(AuthEvent.Loading)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    viewModelScope.launch {
                        if (task.isSuccessful) {
                            _authEvents.send(AuthEvent.Success("Login successful"))
                        } else {
                            _authEvents.send(AuthEvent.Error(task.exception?.message ?: "Login failed"))
                        }
                    }
                }
        }
    }
}