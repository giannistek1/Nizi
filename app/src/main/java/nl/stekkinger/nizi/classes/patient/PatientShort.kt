package nl.stekkinger.nizi.classes.patient

import nl.stekkinger.nizi.classes.diary.ConsumptionShort
import nl.stekkinger.nizi.classes.diary.MyFood
import nl.stekkinger.nizi.classes.dietary.DietaryManagementShort
import nl.stekkinger.nizi.classes.feedback.FeedbackShort
import java.io.Serializable

// Todo: Default values + secondary constructor
data class PatientShort (
    var id: Int = 0,
    var gender: String,
    var date_of_birth: String,
    val doctor: Int,

    val user: Int? = null,
    val feedbacks: ArrayList<FeedbackShort>? = arrayListOf(),
    val dietary_managements: ArrayList<DietaryManagementShort>? = arrayListOf(),
    val my_foods: ArrayList<MyFood>? = arrayListOf(),
    val consumptions: ArrayList<ConsumptionShort>? = arrayListOf(),

    // Unimportant stuff
    val created_at: String? = null,
    val updated_at: String? = null
) : Serializable