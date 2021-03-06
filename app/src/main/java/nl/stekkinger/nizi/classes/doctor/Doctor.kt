package nl.stekkinger.nizi.classes.doctor

import nl.stekkinger.nizi.classes.login.User

data class Doctor(
    val id: Int? = null,
    val location: String,
    val user: User,

    // Unimportant
    val created_at: String? = null,
    val updated_at: String? = null
)