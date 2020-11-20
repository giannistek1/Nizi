package nl.stekkinger.nizi.classes.dietary

import nl.stekkinger.nizi.classes.WeightUnit

data class DietaryRestrictionShort (
    val id: Int,
    val description: String,
    val plural: String,
    val weight_unit: Int
)