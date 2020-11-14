package nl.stekkinger.nizi.classes.patient

import nl.stekkinger.nizi.classes.patient.PatientLogin
import nl.stekkinger.nizi.classes.login.User
import java.io.Serializable

data class AddPatientViewModel (
    val user: User,
    val patient: PatientLogin
) : Serializable