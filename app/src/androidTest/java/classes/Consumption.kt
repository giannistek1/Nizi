package classes

import java.text.SimpleDateFormat

data class Consumption (
    val FoodName: String,
    val KCal: Float,
    val Protein: Float,
    val Fiber: Float,
    val Sodium: Float,
    val Calcium: Float,
    val Amount: Int,
    val WeightUnitId: Int,
    val Date: SimpleDateFormat,
    val PatientId: Int,
    val Id: Int
)