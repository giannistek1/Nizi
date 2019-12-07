package nl.stekkinger.nizi.classes

import java.text.SimpleDateFormat

data class Consumption (
    val foodName: String,
    val kCal: Float,
    val protein: Float,
    val fiber: Float,
    val sodium: Float,
    val calcium: Float,
    val amount: Int,
    val weightUnitId: Int,
    val date: SimpleDateFormat,
    val patientId: Int,
    val id: Int
)