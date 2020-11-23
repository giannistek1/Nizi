package nl.stekkinger.nizi.classes.diary

data class FoodMealComponent(
    val id: Int,
    val name: String,
    val description: String,
    val kcal: Float,
    val protein: Float,
    val potassium: Float,
    val sodium: Float,
    val water: Float,
    val fiber: Float,
    val portion_size: Float,
    val image_url: String
)