package nl.stekkinger.nizi.classes.login

import java.io.Serializable

data class User(
    val id: Int = 0,
    val username: String,
    val password: String? = null, // Only when registering users
    val email: String,
    val first_name: String,
    val last_name: String,
    val role: Int,

    // Which patient OR doctor does this user belong to?
    val patient: Int? = null,
    val doctor: Int? = null,

    // Unimportant stuff
    val created_at: String? = null,
    val updated_at: String? = null,
    val provider: String? = null,
    val confirmed: Boolean? = null,
    val blocked: Boolean? = null
) : Serializable