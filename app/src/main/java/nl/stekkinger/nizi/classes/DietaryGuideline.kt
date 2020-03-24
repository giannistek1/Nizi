package nl.stekkinger.nizi.classes

data class DietaryGuideline(
    var description: String,
    var minimum: Int?,
    var maximum: Int?,
    var amount: Int
)