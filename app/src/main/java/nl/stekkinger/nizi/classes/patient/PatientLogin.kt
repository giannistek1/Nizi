package nl.stekkinger.nizi.classes.patient

import nl.stekkinger.nizi.classes.Consumption
import nl.stekkinger.nizi.classes.Doctor
import nl.stekkinger.nizi.classes.Feedback
import nl.stekkinger.nizi.classes.MyFood
import nl.stekkinger.nizi.classes.dietary.DietaryManagement
import nl.stekkinger.nizi.classes.user.User

data class PatientLogin (
    val id: Int,
    val gender: String,
    val dateOfBirth: String,
    val doctor: Int,
    val user: Int,
    val feedbacks: ArrayList<Feedback>,
    val dietaryManagements: ArrayList<DietaryManagement>,
    val my_food: ArrayList<MyFood>,
    val consumptions: ArrayList<Consumption>,

    // Unimportant stuff
    val created_at: String,
    val updated_at: String
)