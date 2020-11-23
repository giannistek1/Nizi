package nl.stekkinger.nizi.classes.diary

data class MealFood (
    val id: Int,
    val amount: Float,
    val created_at: String? = null,
    val updated_at: String? = null
)