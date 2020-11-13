package nl.stekkinger.nizi.classes.user

import java.io.Serializable

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val role: Role,
    // Unimportant stuff
    val created_at: String,
    val updated_at: String,
    val provider: String,
    val confirmed: Boolean,
    val blocked: Boolean
) : Serializable