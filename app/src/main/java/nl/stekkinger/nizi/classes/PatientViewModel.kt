package nl.stekkinger.nizi.classes

import java.io.Serializable

data class PatientViewModel (
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val weight: Float,
    val gender: String
) : Serializable