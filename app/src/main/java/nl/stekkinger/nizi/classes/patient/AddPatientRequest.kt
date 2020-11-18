package nl.stekkinger.nizi.classes.patient

import java.io.Serializable

data class AddPatientRequest (
    val id: Int? = 0,
    val gender: String = "Man",
    val date_of_birth: String = "",
    val doctor: Int = 1
) : Serializable