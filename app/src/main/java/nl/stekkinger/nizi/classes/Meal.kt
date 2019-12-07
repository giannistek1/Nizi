package nl.stekkinger.nizi.classes

data class Meal (
    val mealId: Int,
    val name: String,
    val kCal: Double,
    val protein: Double,
    val fiber: Double,
    val sodium: Double,
    val calcium: Double,
    val portionSize: Int,
    val weightUnit: String,
    val picture: String
)