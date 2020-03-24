package nl.stekkinger.nizi.classes

data class Meal (
    val MealId: Int,
    val Name: String,
    val PatientId: Int,
    val KCal: Double,
    val Protein: Double,
    val Fiber: Double,
    val Calcium: Double,
    val Sodium: Double,
    val Water: Double,
    val PortionSize: Int,
    val WeightUnit: String,
    val Picture: String
)