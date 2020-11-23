package nl.stekkinger.nizi.classes.diary

import nl.stekkinger.nizi.classes.patient.Patient

data class ConsumptionShort (
    val id: Int = 0,
    val amount: Float,
    val date: String,
    val meal_time: String,
    val patient: Int,
    val weight_unit: Int,
    val food_meal_component: ArrayList<FoodMealComponent> = arrayListOf()
)