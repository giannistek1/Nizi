package nl.stekkinger.nizi.classes.user

import java.io.Serializable

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val provider: String,
    val confirmed: Boolean,
    val blocked: Boolean,
    val role: Role,
    val created_at: String,
    val updated_at: String
) : Serializable