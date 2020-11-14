package nl.stekkinger.nizi.classes.patient

import nl.stekkinger.nizi.classes.Consumption
import nl.stekkinger.nizi.classes.doctor.Doctor
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.MyFood
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.login.User

data class Patient (
    val id: Int? = 0,
    val gender: String,
    val date_of_birth: String,
    val doctor: Doctor,
    val user: User,
    val feedbacks: ArrayList<Feedback>? = arrayListOf(),
    val dietary_managements: ArrayList<DietaryManagement>? = arrayListOf(),
    val my_food: ArrayList<MyFood>? = arrayListOf(),
    val consumptions: ArrayList<Consumption>? = arrayListOf(),

    // Unimportant stuff
    val created_at: String? = null,
    val updated_at: String? = null
)