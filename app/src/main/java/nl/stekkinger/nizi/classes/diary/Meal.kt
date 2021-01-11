package nl.stekkinger.nizi.classes.diary

import nl.stekkinger.nizi.classes.patient.PatientShort
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit

data class Meal (
    val id: Int = 0,
    val name: String = "",
    val food_meal_component: FoodMealComponent,
    val weight_unit: WeightUnit? = null,
    val patient: PatientShort,
    val meal_foods: ArrayList<MealFood>? = arrayListOf()
)