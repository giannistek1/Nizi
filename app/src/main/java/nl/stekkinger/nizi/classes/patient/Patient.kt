package nl.stekkinger.nizi.classes.patient

import nl.stekkinger.nizi.classes.doctor.DoctorShort
import nl.stekkinger.nizi.classes.diary.ConsumptionShort
import nl.stekkinger.nizi.classes.diary.MyFood
import nl.stekkinger.nizi.classes.dietary.DietaryManagementShort
import nl.stekkinger.nizi.classes.feedback.FeedbackShort
import nl.stekkinger.nizi.classes.login.User

data class Patient (
    val id: Int? = null,
    val gender: String,
    val date_of_birth: String,
    val doctor: DoctorShort,
    val user: User? = null,
    val feedbacks: ArrayList<FeedbackShort>? = arrayListOf(),
    val dietary_managements: ArrayList<DietaryManagementShort>? = arrayListOf(),
    val my_foods: ArrayList<MyFood>? = arrayListOf(),
    val consumptions: ArrayList<ConsumptionShort>? = arrayListOf(),

    // Unimportant stuff
    val created_at: String? = null,
    val updated_at: String? = null
)