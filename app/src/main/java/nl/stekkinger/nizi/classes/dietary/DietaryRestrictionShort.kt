package nl.stekkinger.nizi.classes.dietary

data class DietaryRestrictionShort (
    val id: Int,
    val description: String,
    val plural: String,
    val weight_unit: Int
)