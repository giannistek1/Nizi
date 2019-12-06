package nl.stekkinger.nizi.classes

import java.text.SimpleDateFormat

data class WaterConsumptionModel (
    val Id: Int,
    val Amount: Double,
    val Date: SimpleDateFormat,
    val PatientId: Int
)