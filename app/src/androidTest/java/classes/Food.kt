package classes

import java.text.SimpleDateFormat

data class Food (
    val FoodId: Int,
    val Name: String,
    val KCal: Double,
    val Protein: Double,
    val Fiber: Double,
    val Sodium: Double,
    val Calcium: Double,
    val PortionSize: Int,
    val WeightUnit: String,
    val Picture: String
)