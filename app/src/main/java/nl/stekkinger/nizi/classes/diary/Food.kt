package nl.stekkinger.nizi.classes.diary

import nl.stekkinger.nizi.classes.weight_unit.WeightUnit

data class Food (
    val id: Int = 0,
    val my_food: Int = 0,
    var amount: Float = 1f,
    val name: String = "",
    val description: String = "",
    val kcal: Float = 0f,
    val protein: Float = 0f,
    val potassium: Float = 0f,
    val sodium: Float = 0f,
    val water: Float = 0f,
    val fiber: Float = 0f,
    val portion_size: Float = 0f,
    val weight_unit: WeightUnit,
    val weight_amount: Float = 0f,
    val image_url: String = "",
    val foodId: Int = 0,
    val barcode: String = ""
)