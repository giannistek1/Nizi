package nl.stekkinger.nizi.classes.diary

data class Food (
    val id: Int,
    val my_food: Int = 0,
    val name: String,
    val description: String,
    val kcal: Float,
    val protein: Float,
    val potassium: Float,
    val sodium: Float,
    val water: Float,
    val fiber: Float,
    val portion_size: Float,
    val weight_unit: WeightUnit,
    val weight_amount: Float,
    val image_url: String
)