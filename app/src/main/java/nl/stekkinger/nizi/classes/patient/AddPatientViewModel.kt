package nl.stekkinger.nizi.classes.patient

import nl.stekkinger.nizi.classes.login.User
import java.io.Serializable

data class AddPatientViewModel (
    val patient: PatientShort,
    val user: User
) : Serializable