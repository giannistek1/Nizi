package classes

data class Meal (
    val MealId: Int,
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