package nl.stekkinger.nizi.classes.login

import java.io.Serializable

data class LoginRequest(
    val identifier: String, // Username
    val password: String
) : Serializable