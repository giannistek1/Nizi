package nl.stekkinger.nizi.classes

data class DietaryGuideline(
    var description: String,
    var restriction: String, // unneeded?
    var plural: String,
    var minimum: Int,
    var maximum: Int,
    var amount: Int,
    var weightUnit: String
)