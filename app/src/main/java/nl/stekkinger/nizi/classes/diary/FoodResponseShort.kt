package nl.stekkinger.nizi.classes.diary

data class FoodResponseShort (
    val id: Int = 0,
    val name: String,
    val created_at: String? = null,
    val updated_at: String? = null,
    val weight_unit: Int,
    val my_food: Int,
    val food_meal_component: FoodMealComponent
)