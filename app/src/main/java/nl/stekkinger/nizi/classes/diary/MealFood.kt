package nl.stekkinger.nizi.classes.diary

data class MealFood (
    val food: Int,
    val amount: Float,
    val meal: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)