package nl.stekkinger.nizi.classes.dietary

import nl.stekkinger.nizi.classes.weight_unit.WeightUnit

data class DietaryRestriction (
    val id: Int,
    val description: String,
    val plural: String,
    val weight_unit: WeightUnit
)