package nl.stekkinger.nizi.classes

import java.text.SimpleDateFormat

data class Patient (
    val patientId: Int,
    val accountId: Int,
    val doctorId: Int,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val weightInKilograms: Float,
    val guid: String
)