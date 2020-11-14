package nl.stekkinger.nizi.classes.login

import nl.stekkinger.nizi.classes.general.Messages

data class LoginResponse(
    val jwt: String?,
    // UserLogin and User are different because one gives doctor, patient and role objects and the other Ids (ints)
    val user: UserLogin?,

    // ErrorResponse
    val statusCode: Int?,
    val error: String?,
    val message: ArrayList<Messages>?
)