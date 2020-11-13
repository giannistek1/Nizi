package nl.stekkinger.nizi.classes

import nl.stekkinger.nizi.classes.user.User

data class Patient (
    val id: Int,
    val gender: String,
    val dateOfBirth: String,
    val doctor: Doctor,
    val user: User,
    val feedbacks: ArrayList<Feedback>,
    val dietaryManagements: ArrayList<DietaryManagement>,
    val my_food: ArrayList<MyFood>,
    val consumptions: ArrayList<Consumption>,

    // Unimportant stuff
    val created_at: String,
    val updated_at: String
)