package nl.stekkinger.nizi.classes.login

import java.io.Serializable

data class User(
    var id: Int = 0,
    val username: String,
    var password: String? = null, // Only when registering users
    var email: String,
    var first_name: String,
    var last_name: String,
    val role: Int,

    // Which patient OR doctor does this user belong to?
    var patient: Int? = null,
    var doctor: Int? = null,

    // Unimportant stuff
    val created_at: String? = null,
    val updated_at: String? = null,
    val provider: String? = null,
    val confirmed: Boolean? = null,
    val blocked: Boolean? = null
) : Serializable