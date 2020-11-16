package nl.stekkinger.nizi.classes.patient

import nl.stekkinger.nizi.classes.Consumption
import nl.stekkinger.nizi.classes.doctor.Doctor
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.MyFood
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.login.User
import java.io.Serializable

data class AddPatientRequest (
    val id: Int? = 0,
    val gender: String = "Man",
    val date_of_birth: String = "",
    val doctor: Int = 1
) : Serializable