package nl.stekkinger.nizi.classes.patient

import nl.stekkinger.nizi.classes.Consumption
import nl.stekkinger.nizi.classes.Doctor
import nl.stekkinger.nizi.classes.Feedback
import nl.stekkinger.nizi.classes.MyFood
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.user.User

data class Patient (
    val id: Int,
    val gender: String,
    val date_of_birth: String,
    val doctor: Doctor,
    val user: User,
    val feedbacks: ArrayList<Feedback>,
    val dietary_managements: ArrayList<DietaryManagement>,
    val my_food: ArrayList<MyFood>,
    val consumptions: ArrayList<Consumption>,

    // Unimportant stuff
    val created_at: String,
    val updated_at: String
)