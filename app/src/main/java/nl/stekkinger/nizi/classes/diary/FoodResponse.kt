package nl.stekkinger.nizi.classes.diary

data class FoodResponse (
    val id: Int = 0,
    val name: String,
    val created_at: String? = null,
    val updated_at: String? = null,
    val weight_unit: WeightUnit,
    val my_food: MyFood,
    val food_meal_component: FoodMealComponent,
    val meal_foods: ArrayList<MealFood> = arrayListOf()
)