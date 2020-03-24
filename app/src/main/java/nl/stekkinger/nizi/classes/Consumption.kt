package nl.stekkinger.nizi.classes

import java.text.SimpleDateFormat

data class Consumption (
    val FoodName: String,
    val KCal: Float,
    val Protein: Float,
    val Fiber: Float,
    val Calium: Float,
    val Sodium: Float,
    val Water: Float,
    val Amount: Int,
    val MealTime: String,
    val WeightUnitId: Int,
    val Date: String,
    val PatientId: Int,
    val Id: Int
)