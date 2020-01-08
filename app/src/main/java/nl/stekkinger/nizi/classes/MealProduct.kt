package nl.stekkinger.nizi.classes

data class MealProduct (
    val Name: String,
    val KCal: Float,
    val Protein: Float,
    val Fiber: Float,
    val Calcium: Float,
    val Sodium: Float,
    val PortionSize: Int,
    val WeightUnit: String
)