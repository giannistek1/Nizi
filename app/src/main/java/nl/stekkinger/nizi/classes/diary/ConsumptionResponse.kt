package nl.stekkinger.nizi.classes.diary

import nl.stekkinger.nizi.classes.weight_unit.WeightUnit

data class ConsumptionResponse (
    val id: Int = 0,
    val amount: Float,
    val date: String,
    val meal_time: String,
    val patient: PatientDiary,
    val weight_unit: WeightUnit,
    val food_meal_component: FoodMealComponent
)