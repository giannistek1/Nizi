package nl.stekkinger.nizi.classes.login

import nl.stekkinger.nizi.classes.user.User
import nl.stekkinger.nizi.classes.general.Messages

data class LoginResponse(
    val jwt: String?,
    val user: User?,

    // ErrorResponse
    val statusCode: Int?,
    val error: String?,
    val message: ArrayList<Messages>?
)