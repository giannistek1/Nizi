package nl.stekkinger.nizi.classes.diary

import java.io.Serializable

data class FoodMealComponent(
    val id: Int = 0,
    val name: String,
    val description: String,
    val kcal: Float,
    val protein: Float,
    val potassium: Float,
    val sodium: Float,
    val water: Float,
    val fiber: Float,
    val portion_size: Float,
    val image_url: String,
    val foodId: Int
) : Serializable