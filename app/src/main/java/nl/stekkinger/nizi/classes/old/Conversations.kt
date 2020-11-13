package nl.stekkinger.nizi.classes.old

import nl.stekkinger.nizi.classes.Consumptions

data class Conversation (
    val WeightUnit: Consumptions.WeightUnit,
    val Error: Boolean,
    val Id: Int,
    val Amount: Double,
    val Date: String,
    val PatientId: Int,
    val Comment: String
)