package nl.stekkinger.nizi.classes

import java.text.SimpleDateFormat
class Consumptions {

    data class Result (
        val Consumptions: ArrayList<Consumption> = ArrayList(),
        val KCalTotal: Float,
        val ProteinTotal: Float,
        val FiberTotal: Float,
        val CaliumTotal: Float,
        val SodiumTotal: Float,
        val WaterTotal: Float
    )

//    data class Consumption (
//        val FoodName: String,
//        val KCal: Float,
//        val Protein: Float,
//        val Fiber: Float,
//        val Calium: Float,
//        val Sodium: Float,
//        val Water: Double,
//        val Amount: Int,
//        val MealTime: String,
//        val WeightUnitId: Int,
//        val Date: String,
//        val PatientId: Int,
//        val Id: Int
//    )
    data class WeightUnit (
        val Id: Int,
        val Unit: String,
        val Short: String
    )
}
