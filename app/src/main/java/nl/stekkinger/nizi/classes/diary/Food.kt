package nl.stekkinger.nizi.classes.diary

import nl.stekkinger.nizi.classes.weight_unit.WeightUnit

data class Food (
    val id: Int,
    val my_food: Int = 0,
    var amount: Float = 1f,
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
    val image_url: String,
    val foodId: Int
)