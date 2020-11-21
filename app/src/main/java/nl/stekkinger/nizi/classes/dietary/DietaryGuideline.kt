package nl.stekkinger.nizi.classes.dietary

import java.io.Serializable

// Easy compact version
data class DietaryGuideline(
    var id: Int,
    var description: String, // calorie/vocht etc
    var plural: String, // calorieën
    var minimum: Int,
    var maximum: Int,
    var amount: Int,
    var weightUnit: String
) : Serializable