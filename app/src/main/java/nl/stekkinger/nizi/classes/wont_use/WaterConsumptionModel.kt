package nl.stekkinger.nizi.classes.wont_use

import java.text.SimpleDateFormat

data class WaterConsumptionModel (
    val id: Int,
    val amount: Double,
    val date: SimpleDateFormat,
    val patientId: Int
)