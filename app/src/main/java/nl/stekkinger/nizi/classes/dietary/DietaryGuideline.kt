package nl.stekkinger.nizi.classes.dietary

import java.io.Serializable

// Easy compact version
data class DietaryGuideline(
    var description: String, // caloriebeperking
    var restriction: String, // unneeded? calorie/eiwit etc
    var plural: String, // calorieën
    var minimum: Int,
    var maximum: Int,
    var amount: Int,
    var weightUnit: String
) : Serializable