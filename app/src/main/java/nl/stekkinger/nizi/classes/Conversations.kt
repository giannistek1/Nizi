package nl.stekkinger.nizi.classes

import java.text.SimpleDateFormat

data class Conversation (
    val Id: Int,
    val Amount: Double,
    val Date: SimpleDateFormat,
    val PatientId: Int,
    val Comment: String
)