package nl.stekkinger.nizi.classes

import nl.stekkinger.nizi.classes.diary.FoodMealComponent
import nl.stekkinger.nizi.classes.weight_unit.WeightUnit
import java.io.Serializable
import java.text.SimpleDateFormat

data class Consumption (
    val id: Int = 0,
    val amount: Float,
    val date: String,
    val meal_time: String,
    val patient: Int,
    val weight_unit: WeightUnit,
    val food_meal_component: FoodMealComponent
)
// Todo remove old code
//data class Consumption (
//    val FoodName: String,
//    val KCal: Float,
//    val Protein: Float,
//    val Fiber: Float,
//    val Calium: Float,
//    val Sodium: Float,
//    val Water: Float,
//    val Amount: Int,
//    val MealTime: String,
//    val WeightUnitId: Int,
//    val Date: String,
//    val PatientId: Int,
//    val ConsumptionId: Int
//) : Serializable