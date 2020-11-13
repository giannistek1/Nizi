package nl.stekkinger.nizi.classes

import java.text.SimpleDateFormat

data class Conversation (
    val WeightUnit: Consumptions.WeightUnit,
    val Error: Boolean,
    val Id: Int,
    val Amount: Double,
    val Date: String,
    val PatientId: Int,
    val Comment: String
)