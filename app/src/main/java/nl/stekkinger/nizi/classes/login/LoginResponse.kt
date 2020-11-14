package nl.stekkinger.nizi.classes.login

import nl.stekkinger.nizi.classes.Doctor
import nl.stekkinger.nizi.classes.user.User
import nl.stekkinger.nizi.classes.general.Messages
import nl.stekkinger.nizi.classes.patient.Patient
import nl.stekkinger.nizi.classes.user.UserLogin

data class LoginResponse(
    val jwt: String?,
    // UserLogin and User are different because one gives doctor and patient objects and the other Ids (ints)
    val user: UserLogin?,

    // ErrorResponse
    val statusCode: Int?,
    val error: String?,
    val message: ArrayList<Messages>?
)