package uk.ac.tees.mad.ridehive.model

data class Users(
    val createdAt: Long? = null,
    val email: String = "",
    val firstName: String = "",
    val lastName: String ="",
    val photoUrl: String? = null,
    val uid: String? = null
)
