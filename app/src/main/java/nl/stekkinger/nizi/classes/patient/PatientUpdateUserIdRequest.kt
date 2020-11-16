package nl.stekkinger.nizi.classes.patient

import java.io.Serializable

data class PatientUpdateUserIdRequest(
    val userId: Int
) : Serializable