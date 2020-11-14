package nl.stekkinger.nizi.classes.patient

import nl.stekkinger.nizi.classes.Consumption
import nl.stekkinger.nizi.classes.feedback.Feedback
import nl.stekkinger.nizi.classes.MyFood
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import java.io.Serializable

data class PatientLogin (
    val id: Int = 0,
    val gender: String,
    val date_of_birth: String,
    val doctor: Int,
    val user: Int? = null,
    val feedbacks: ArrayList<Feedback> = arrayListOf(),
    val dietary_managements: ArrayList<DietaryManagement> = arrayListOf(),
    val my_food: ArrayList<MyFood> = arrayListOf(),
    val consumptions: ArrayList<Consumption> = arrayListOf(),

    // Unimportant stuff
    val created_at: String? = null,
    val updated_at: String? = null
) : Serializable