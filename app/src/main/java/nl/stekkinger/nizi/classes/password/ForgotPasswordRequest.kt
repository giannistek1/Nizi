package nl.stekkinger.nizi.classes.password

import java.io.Serializable

data class ForgotPasswordRequest(
    val email: String
) : Serializable