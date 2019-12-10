package nl.stekkinger.nizi.classes

data class Food (
    val FoodId: Int,
    val Name: String,
    val KCal: Double,
    val Protein: Double,
    val Fiber: Double,
    val Calcium: Double,
    val Sodium: Double,
    val PortionSize: Double,
    val WeightUnit: String,
    val Picture: String
)