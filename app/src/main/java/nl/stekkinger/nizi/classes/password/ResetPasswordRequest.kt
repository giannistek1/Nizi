package nl.stekkinger.nizi.classes.password

import java.io.Serializable

data class ResetPasswordRequest(
    val code: String,
    val password: String,
    val passwordConfirmation: String
) : Serializable