package nl.stekkinger.nizi.classes.diary

import nl.stekkinger.nizi.classes.weight_unit.WeightUnit

data class MealProduct (
    val amount: Float,
    val food_meal_component: FoodMealComponent,
    val weight_unit: WeightUnit
)