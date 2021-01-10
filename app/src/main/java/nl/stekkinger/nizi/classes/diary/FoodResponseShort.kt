package nl.stekkinger.nizi.classes.diary

data class FoodResponseShort (
    val id: Int = 0,
    val name: String,
    val created_at: String? = null,
    val updated_at: String? = null,
    val weight_unit: Int,
    val my_food: Int,
    val meal_food: Int? = null,
    val food_meal_component: FoodMealComponent,
    val barcode: String
)